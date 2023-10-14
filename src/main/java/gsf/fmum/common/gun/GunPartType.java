package gsf.fmum.common.gun;

import com.google.gson.annotations.SerializedName;
import gsf.fmum.client.player.PlayerPatchClient;
import gsf.fmum.client.render.IAnimator;
import gsf.fmum.client.render.Model;
import gsf.fmum.common.FMUM;
import gsf.fmum.common.item.IEquippedItem;
import gsf.fmum.common.item.IItemType;
import gsf.fmum.common.item.ItemType;
import gsf.fmum.common.load.IContentBuildContext;
import gsf.fmum.common.module.IModule;
import gsf.fmum.common.module.IModuleSlot;
import gsf.fmum.common.module.IModuleType;
import gsf.fmum.common.module.Module;
import gsf.fmum.common.module.ModuleSnapshot;
import gsf.fmum.common.pack.IContentPackFactory.IPostLoadContext;
import gsf.fmum.common.paintjob.IPaintableType;
import gsf.fmum.common.paintjob.IPaintjob;
import gsf.fmum.util.Category;
import gsf.fmum.util.GLUtil;
import gsf.fmum.util.Mat4f;
import gsf.fmum.util.MathUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
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
import scala.util.Random;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class GunPartType extends ItemType implements IModuleType, IPaintableType
{
	private static final float[] DEFAULT_OFFSETS = { 0.0F };
	
	
	protected Category category;
	
	protected float param_scale = 1.0F;
	
	protected List< IModuleSlot > slots = Collections.emptyList();
	
	protected ModuleSnapshot snapshot = ModuleSnapshot.DEFAULT;
	protected transient NBTTagCompound snapshot_nbt;
	
	protected transient Item raw_item;
	protected transient NBTTagCompound raw_root_nbt;
	
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
		
		this.raw_item = this._createRawItem( ctx );
		this.raw_root_nbt = this._genRawRootNBT( ctx );
		
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
		
		ctx.regisPostLoadCallback( ctx_ ->
			this.snapshot_nbt = this._genSnapshotNBT( ctx_ ) );
	}
	
	@Override
	@SideOnly( Side.CLIENT )
	public void buildClientSide( IContentBuildContext ctx )
	{
		super.buildClientSide( ctx );
		
		ctx.regisMeshLoadCallback( this.model::loadMesh );
	}
	
	@Override
	public IModule createRawModule()
	{
		// FIXME: Specialized so that it will not break on snapshot restore.
		final ItemStack stack = new ItemStack( this.raw_item );
		return this.getItem( stack );
	}
	
	@Override
	public IModule deserializeFrom( NBTTagCompound nbt )
	{
		final _GunPart< ? > self = new _GunPart<>( nbt );
		self.deserializeNBT( nbt );
		return self;
	}
	
	@Override
	public IGunPart getItem( ItemStack stack ) {
		return ( IGunPart ) super.getItem( stack );
	}
	
	@Override
	public void injectPaintjob( IPaintjob paintjob ) {
		this.paintjobs.add( paintjob );
	}
	
	@Override
	protected Item _createItem( IContentBuildContext ctx ) {
		return new _ItemGunPart();
	}
	
	protected NBTTagCompound _genSnapshotNBT( IPostLoadContext ctx )
	{
		final IModule self = this.createRawModule();
		this.snapshot.restore( self, name -> {
			final Optional< IModule > module = IModuleType
				.REGISTRY.lookup( name ).map( IModuleType::createRawModule );
			if ( !module.isPresent() ) {
				FMUM.MOD.logError( "fmum.fail_to_find_module", this, name );
			}
			return module;
		} );
		return self.serializeNBT();
	}
	
	protected Item _createRawItem( IContentBuildContext ctx )
	{
		return new _ItemGunPart()
		{
			@Override
			public ICapabilityProvider initCapabilities(
				ItemStack stack,
				@Nullable NBTTagCompound cap_tag
			) { return GunPartType.this._initRawItemStack( stack ); }
		};
	}
	
	protected final ICapabilityProvider _initRawItemStack( ItemStack stack )
	{
		final NBTTagCompound stack_tag = new NBTTagCompound();
		final int stack_id = MathUtil.RAND.nextInt();
		stack_tag.setInteger( GunPartWrapper.STACK_ID_TAG, stack_id );
		stack.setTagCompound( stack_tag );
		
		final NBTTagCompound root_tag = GunPartType.this.raw_root_nbt.copy();
		final IModule self = GunPartType.this.deserializeFrom( root_tag );
		final ICapabilityProvider wrapper = GunPartType.this._wrap( self, stack );
		// TODO: Refresh event subscribe.
		self._syncNBTTag();
		return wrapper;
	}
	
	protected NBTTagCompound _genRawRootNBT( IContentBuildContext ctx ) {
		return new _GunPart<>().serializeNBT();
	}
	
	protected ICapabilityProvider _wrap( IModule self, ItemStack stack )
	{
		final _GunPart< ? > gun_part = ( _GunPart< ? > ) self;
		return new GunPartWrapper<>( gun_part, stack );
	}
	
	@Override
	protected String _typeHint() {
		return "GUN_PART";
	}
	
	
	protected class _ItemGunPart extends _FMUMItem
	{
		protected _ItemGunPart()
		{
			// Set it as not stackable.
			this.setMaxStackSize( 1 );
			// TODO: Handle paintjobs.
		}
		
		@Override
		public final IItemType type() {
			return GunPartType.this;
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
				
				final IModule self = IModule.deserializeFrom( root_tag );
				final ICapabilityProvider wrapper = GunPartType.this._wrap( self, stack );
				// TODO: self.refreshEventSubscribe();
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
			
			final NBTTagCompound root_tag = GunPartType.this.snapshot_nbt.copy();
			final IModule self = GunPartType.this.deserializeFrom( root_tag );
			final ICapabilityProvider wrapper = GunPartType.this._wrap( self, stack );
			// TODO: Refresh event subscribe.
			self._syncNBTTag();
			return wrapper;
		}
		
		@Override
		public void readNBTShareTag( @Nonnull ItemStack stack, NBTTagCompound nbt )
		{
			super.readNBTShareTag( stack, nbt );
			
			// See GunPartWrapper#syncNBTTag().
			final NBTTagCompound root_tag = nbt.getCompoundTag( GunPartWrapper.ROOT_TAG );
			final IModule root = IModule.deserializeFrom( root_tag );
			
			final IModule wrapper = GunPartType.this.getItem( stack );
			wrapper._setBase( root, -1 );  // See GunPartWrapper#_setParent(...).
			// TODO: wrapper._refreshEventSubscribe();
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
	}
	
	
	protected class _GunPart< T extends IGunPart >
		extends Module< T > implements IGunPart
	{
		protected short offset;
		protected short step;
		
		protected _GunPart() { }
		
		protected _GunPart( NBTTagCompound nbt )
		{
			super( nbt );
			
			// FIXME
		}
		
		@Override
		public IEquippedItem< ? > onTakeOut( EntityPlayer player, EnumHand hand )
		{
			final NBTTagCompound stack_tag = this.base.itemStack().getTagCompound();
			final int stack_id = stack_tag.getInteger( GunPartWrapper.STACK_ID_TAG );
			
			final boolean is_logic_server = !player.world.isRemote;
			if ( is_logic_server )
			{
				// This is required because we have no way to assign a new id \
				// for a copied gun stack without using the mixin, which means \
				// two guns in player's inventory could have essentially \
				// identical id! This hack generates a new id for gun in hand \
				// on taking out, which can not fully settle this problem, but \
				// should be enough to make it work in most of the time.
				
				// Only update id on server side.
				final int seed = stack_id + player.inventory.currentItem;
				final int new_stack_id = new Random( seed ).nextInt();
				stack_tag.setInteger( GunPartWrapper.STACK_ID_TAG, new_stack_id );
			}
			else  // On client side.
			{
				// Ignore if id changed to the new id updated by server.
				final IEquippedItem< ? > prev_equipped =
					PlayerPatchClient.instance.getEquipped( hand );
				final int prev_stack_id = prev_equipped.item().stackId();
				final int seed = prev_stack_id + player.inventory.currentItem;
				final int new_stack_id = new Random( seed ).nextInt();
				final boolean is_id_update = stack_id == new_stack_id;
				if ( is_id_update )
				{
					final ItemStack stack = this.base.itemStack();
					final NBTTagCompound stack_nbt = stack.getTagCompound();
					assert stack_nbt != null;
					stack_nbt.setInteger( GunPartWrapper.STACK_ID_TAG, stack_id );
				}
			}
			
			return this._createEquipped( player, hand );
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
		public IModule _onBeingRemoved()
		{
			final ItemStack stack = new ItemStack( GunPartType.this.item );
			final IModule wrapper = GunPartType.this.getItem( stack );
			wrapper._setBase( this, -1 );
			
			this._syncNBTTag();
			// TODO: Update module event subscribe.
			return wrapper;
		}
		
		@Override
		public void _getInstalledTransform(
			IModule installed,
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
		
		protected IEquippedItem< ? >
			_createEquipped( EntityPlayer player, EnumHand hand )
		{ return new EquippedGunPart( player, hand ); }
		
		protected class EquippedGunPart implements IEquippedItem< IGunPart >
		{
			protected EquippedGunPart( EntityPlayer player, EnumHand hand )
			{
			
			}
			
			@Override
			public IGunPart item() {
				return _GunPart.this;
			}
			
			@Override
			@SideOnly( Side.CLIENT )
			public void prepareRenderInHand( EnumHand enumHand )
			{
				RenderQueue.NORMAL.clear();
				RenderQueue.PRIORITIZED.clear();
				_GunPart.this._prepareRenderInHand(
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
