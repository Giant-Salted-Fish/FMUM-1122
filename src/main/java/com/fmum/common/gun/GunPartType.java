package com.fmum.common.gun;

import com.fmum.client.player.PlayerPatchClient;
import com.fmum.client.render.IAnimator;
import com.fmum.common.FMUM;
import com.fmum.common.item.IEquippedItem;
import com.fmum.common.item.ItemType;
import com.fmum.common.load.IContentBuildContext;
import com.fmum.client.render.Model;
import com.fmum.common.module.IModule;
import com.fmum.common.module.IModuleSlot;
import com.fmum.common.module.IModuleType;
import com.fmum.common.module.Module;
import com.fmum.common.module.ModuleSnapshot;
import com.fmum.common.pack.IContentPackFactory.IPostLoadContext;
import com.fmum.common.paintjob.IPaintableType;
import com.fmum.common.paintjob.IPaintjob;
import com.fmum.util.Category;
import com.fmum.util.GLUtil;
import com.fmum.util.Mat4f;
import com.fmum.util.MathUtil;
import com.google.gson.annotations.SerializedName;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class GunPartType extends ItemType implements IModuleType, IPaintableType
{
	private static final float[] DEFAULT_OFFSETS = { 0.0F };
	
	
	protected Category category;
	
	protected float param_scale = 1.0F;
	
	protected List< IModuleSlot > slots = Collections.emptyList();
	
	protected ModuleSnapshot snapshot = ModuleSnapshot.DEFAULT;
	protected transient NBTTagCompound snapshot_nbt;
	
	protected float[] offsets = DEFAULT_OFFSETS;
	
	protected List< IPaintjob > paintjobs = Collections.emptyList();
	
	@SideOnly( Side.CLIENT )
	@SerializedName( value = "model", alternate = "mesh" )
	protected Model model;
	
	@SideOnly( Side.CLIENT )
	protected String module_animation_channel;
	
	@Override
	public void buildServerSide( IContentBuildContext ctx )
	{
		super.buildServerSide( ctx );
		
		IModuleType.REGISTRY.regis( this );
		IPaintableType.REGISTRY.regis( this );
		
		// Set it as not stackable.
		this.setMaxStackSize( 1 );
		
		// Check member variable setup.
		this.category = Optional.ofNullable( this.category )
			.orElseGet( () -> new Category( this.name ) );
		// TODO: Modify indicator.
		
		// Regis itself as the default paintjob.
		if ( this.paintjobs.isEmpty() ) {
			this.paintjobs = new ArrayList<>();
		}
		this.paintjobs.add( 0, () -> this.texture );
		
		// Apply param scale.
		this.slots.forEach( slot -> slot.scaleParam( this.param_scale ) );
		// TODO: Scale hit boxes.
		
		ctx.regisPostLoadCallback( this::_setupSnapshotNBT );
	}
	
	@Override
	@SideOnly( Side.CLIENT )
	public void buildClientSide( IContentBuildContext ctx )
	{
		super.buildClientSide( ctx );
		
		ctx.regisMeshLoadCallback( this.model::loadMesh );
	}
	
	@Override
	public ICapabilityProvider initCapabilities(
		ItemStack stack,
		@Nullable NBTTagCompound cap_tag
	) {
		// 4 case to handle: \
		// has-stack_tag | has-cap_tag: {ItemStack#ItemStack(NBTTagCompound)} \
		// has-stack_tag | no--cap_tag: \
		// no--stack_tag | has-cap_tag: {ItemStack#copy()} \
		// no--stack_tag | no--cap_tag: {new ItemStack(...)}, {PacketBuffer#readItemStack()} \
		final NBTTagCompound stack_tag = stack.getTagCompound();
		final boolean has_cap_tag = cap_tag != null;
		if ( has_cap_tag )
		{
			// 2 cases possible:
			// no--stack_tag | has-cap_tag: {ItemStack#copy()}.
			// has-stack_tag | has-cap_tag: {ItemStack#ItemStack(NBTTagCompound)}.
			final NBTTagCompound nbt = cap_tag.getCompoundTag( "Parent" );
			
			// Remove "Parent" tag to prevent repeated deserialization.
			// See CapabilityDispatcher#deserializeNBT(NBTTagCompound).
			cap_tag.removeTag( "Parent" );
			
			// Has to copy before use if it is the first case as the \
			// capability tag provided here could be the same as the bounden \
			// tag of the copy target.
			final boolean is_stack_copy = stack_tag == null;
			final NBTTagCompound root_tag = is_stack_copy ? nbt.copy() : nbt;
			
			final IModule< ? > self = IModule.deserializeFrom( root_tag );
			final ICapabilityProvider wrapper = this._wrap( self, stack );
//			TODO: self.refreshEventSubscribe();
			return wrapper;
		}
		
		// has-stack_tag | no--cap_tag: should never happen.
		assert stack_tag != null;
		
		// no--stackTag | no--capTag: {new ItemStack(...)}, {PacketBuffer#readItemStack()}
		// We basically have no way to distinguish from these two cases. But \
		// it will work fine if we simply deserialize and setup it with the \
		// compiled snapshot NBT. That is because #readNBTShareTag(ItemStack, NBTTagCompound) \
		// will later be called for the network packet case. The downside is \
		// that it will actually deserialize twice for the network packet case.
		final NBTTagCompound new_stack_tag = new NBTTagCompound();
		final int stack_id = MathUtil.RAND.nextInt();
		new_stack_tag.setInteger( GunPartWrapper.STACK_ID_TAG, stack_id );
		stack.setTagCompound( new_stack_tag );
		
		final NBTTagCompound root_tag = this.snapshot_nbt.copy();
		final IModule< ? > self = this.deserializeFrom( root_tag );
		final ICapabilityProvider wrapper = this._wrap( self, stack );
		// TODO: Refresh event subscribe.
		self._syncNBTTag();
		return wrapper;
	}
	
	@Override
	public void readNBTShareTag( ItemStack stack, NBTTagCompound nbt )
	{
		super.readNBTShareTag( stack, nbt );
		
		// See GunPartWrapper#syncNBTTag().
		final NBTTagCompound root_tag = nbt.getCompoundTag( GunPartWrapper.ROOT_TAG );
		final IModule< ? > root = IModule.deserializeFrom( root_tag );
		
		final IModule< ? > wrapper = ( IModule< ? > ) this.getItem( stack );
		wrapper._setBase( root, -1 );  // See GunPartWrapper#_setParent(...).
//		TODO: wrapper._refreshEventSubscribe();
	}
	
	@Override
	public void injectPaintjob( IPaintjob paintjob ) {
		this.paintjobs.add( paintjob );
	}
	
	/**
	 * <p> {@inheritDoc} </p>
	 *
	 * <p> In default avoid to break the block when holding a this item in
	 * survive mode. </p>
	 */
	@Override
	public boolean onBlockStartBreak(
		@Nonnull ItemStack itemstack,
		@Nonnull BlockPos pos,
		@Nonnull EntityPlayer player
	) { return true; }
	
	/**
	 * <p> {@inheritDoc} </p>
	 *
	 * <p> In default avoid to break the block when holding this item in creative mode. </p>
	 */
	@Override
	public boolean canDestroyBlockInCreative(
		@Nonnull World world,
		@Nonnull BlockPos pos,
		@Nonnull ItemStack stack,
		@Nonnull EntityPlayer player
	) { return false; }
	
	protected void _setupSnapshotNBT( IPostLoadContext ctx )
	{
		final IModule< ? > self = this.createRawModule();
		this.snapshot.restore( self, name -> {
			final Optional< IModule< ? > > module = IModuleType
				.REGISTRY.lookup( name ).map( IModuleType::createRawModule );
			if ( !module.isPresent() ) {
				FMUM.MOD.logError( "fmum.fail_to_find_module", this, name );
			}
			return module;
		} );
	}
	
	protected ICapabilityProvider _wrap( IModule< ? > self, ItemStack stack )
	{
		final GunPart< ? > gun_part = ( GunPart< ? > ) self;
		return new GunPartWrapper<>( gun_part, stack );
	}
	
	@Override
	protected String _typeHint() {
		return "GUN_PART";
	}
	
	
	protected class GunPart< I extends IGunPart< ? extends I > >
		extends Module< I > implements IGunPart< I >
	{
		protected short offset;
		protected short step;
		
		protected GunPart() { }
		
		protected GunPart( NBTTagCompound nbt )
		{
			super( nbt );
			
			// FIXME
		}
		
		@Override
		public int stackId() {
			throw new RuntimeException();
		}
		
		@Override
		public String name() {
			return GunPartType.this.name;
		}
		
		@Override
		public Category category() {
			return GunPartType.this.category;
		}
		
		@Override
		public int paintjobCount() {
			return GunPartType.this.paintjobs.size();
		}
		
		@Override
		public int slotCount() {
			return GunPartType.this.slots.size();
		}
		
		@Override
		public IModuleSlot getSlot( int idx ) {
			return GunPartType.this.slots.get( idx );
		}
		
		@Override
		public int offsetCount() {
			return GunPartType.this.offsets.length;
		}
		
		@Override
		public int offset() {
			return this.offset;
		}
		
		@Override
		public int step() {
			return this.step;
		}
		
		@Override
		public void _getInstalledTransform(
			IModule< ? > installed,
			IAnimator animator,
			Mat4f dst
		) {
			// Copy self transform.
			this.mat.set( dst );
			
			// Apply Slot transform.
			final IModuleSlot slot = GunPartType.this
				.slots.get( installed.installationSlotIdx() );
			slot.applyTransform( installed, dst );
		}
		
		@Override
		@SideOnly( Side.CLIENT )
		public void _prepareRenderInHand(
			IAnimator animator,
			IInHandRenderContext ctx
		) {
			this.base._getInstalledTransform( this, animator, this.mat );
			animator.applyChannel( GunPartType.this.module_animation_channel, this.mat );
			
			// TODO: We can buffer animator so no instance will be created for this closure?
			ctx.addQueuedRenderer( () -> {
				GL11.glPushMatrix(); {
				GLUtil.glMulMatrix( this.mat );
				GLUtil.bindTexture( this._texture() );
				GunPartType.this.model.render();
				} GL11.glPopMatrix();
			} );
			
			this.installed_modules.forEach(
				mod -> mod._prepareRenderInHand( animator, ctx ) );
		}
		
		@Override
		public void deserializeNBT( NBTTagCompound nbt )
		{
			super.deserializeNBT( nbt );
			
			final int[] data = nbt.getIntArray( DATA_TAG );
			final int value = data[ super._dataSize() ];
			this.offset = ( short ) value;
			this.step = ( short ) ( value >>> 16 );
		}
		
		@Override
		public String toString() {
			return String.format( "Item<%s>", GunPartType.this );
		}
		
		protected void _setOffsetAndStep( int offset, int step )
		{
			this.offset = ( short ) offset;
			this.step = ( short ) step;
			final int[] data = this.nbt.getIntArray( DATA_TAG );
			data[ super._dataSize() ] = 0xFFFF & offset | step << 16;
			
			// TODO: What animator to use?
			this.base._getInstalledTransform( this, IAnimator.NONE, this.mat );
		}
		
		@Override
		protected int _id() {
			return IModuleType.REGISTRY.getID( GunPartType.this );
		}
		
		@Override
		protected int _dataSize() {
			return 1 + super._dataSize();
		}
		
		@SideOnly( Side.CLIENT )
		protected ResourceLocation _texture() {
			return GunPartType.this.paintjobs.get( this.paintjob ).texture();
		}
		
		
		protected class EquippedGunPart
			implements IEquippedItem< IGunPart< ? > >
		{
			@Override
			public IGunPart< ? > item() {
				return GunPart.this;
			}
			
			@Override
			@SideOnly( Side.CLIENT )
			public void prepareRenderInHand( EnumHand enumHand )
			{
				RenderQueue.NORMAL.clear();
				RenderQueue.PRIORITIZED.clear();
				GunPart.this._prepareRenderInHand(
					IAnimator.NONE, RenderQueue.IN_HAND_CONTEXT );
			}
			
			@Override
			@SideOnly( Side.CLIENT )
			public boolean onRenderHand( EnumHand hand )
			{
				PlayerPatchClient.instance.setupInHandAndRender( () -> {
					RenderQueue.NORMAL.forEach( Runnable::run );
					RenderQueue.PRIORITIZED.sort(
						( r0, r1 ) -> Float.compare( r0.priority(), r1.priority() ) );
					RenderQueue.PRIORITIZED.forEach( IPrioritizedRenderer::render );
					
					// Clear to release references as quick as possible.
					RenderQueue.NORMAL.clear();
					RenderQueue.PRIORITIZED.clear();
				} );
				
				final boolean cancel_vanilla_hand_render = true;
				return cancel_vanilla_hand_render;
			}
			
			@Override
			@SideOnly( Side.CLIENT )
			public boolean onRenderSpecificHand( EnumHand hand ) {
				return true;
			}
		}
	}
}
