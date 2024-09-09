package com.fmum.gunpart;

import com.fmum.FMUM;
import com.fmum.SyncConfig;
import com.fmum.item.IEquippedItem;
import com.fmum.item.IItem;
import com.fmum.item.IItemType;
import com.fmum.item.ItemCategory;
import com.fmum.item.ItemType;
import com.fmum.load.IContentBuildContext;
import com.fmum.load.IMeshLoadContext;
import com.fmum.module.IModifyContext;
import com.fmum.module.IModifyPreview;
import com.fmum.module.IModule;
import com.fmum.module.IModuleType;
import com.fmum.module.Module;
import com.fmum.paintjob.IPaintableType;
import com.fmum.paintjob.IPaintjob;
import com.fmum.render.AnimatedModel;
import com.fmum.render.IAnimator;
import com.fmum.render.IRenderCallback;
import com.fmum.render.ModelPath;
import com.fmum.render.Texture;
import com.google.gson.JsonObject;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.mojang.realmsclient.util.Pair;
import gsf.util.animation.IAnimation;
import gsf.util.animation.IPoseSetup;
import gsf.util.math.Mat4f;
import gsf.util.math.Quat4f;
import gsf.util.math.Vec3f;
import gsf.util.render.GLUtil;
import gsf.util.render.Mesh;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.event.RegistryEvent.Register;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

public class GunPartType extends ItemType implements IModuleType, IPaintableType
{
	@Expose
	protected ItemCategory category;
	
	@Expose
	protected RailSlot[] slots = { };
	
	@Expose
	protected float[] offsets = { };
	
	@Expose
	protected List< IPaintjob > paintjobs = Collections.emptyList();
	
	@Expose
	protected BiFunction< IModule, Function< String, Optional< IModule > >, IModule >
		default_setup = ( module, factory ) -> module;
	
	
	// >>> Render Setup <<<
	@Expose
	@SideOnly( Side.CLIENT )
	@SerializedName( value = "models", alternate = "model" )
	protected AnimatedModel[] models;
	
	@Expose
	@SideOnly( Side.CLIENT )
	protected Texture texture;
	
	@Expose
	@SideOnly( Side.CLIENT )
	protected float fp_scale;
	
	@Expose
	@SideOnly( Side.CLIENT )
	protected Vec3f fp_pos;
	
	@Expose
	@SideOnly( Side.CLIENT )
	protected Quat4f fp_rot;
	
	@Expose
	@SideOnly( Side.CLIENT )
	protected String mod_anim_channel;
	
	@Expose
	@SideOnly( Side.CLIENT )
	protected Vec3f modify_pos;
	
	@Expose
	@SideOnly( Side.CLIENT )
	@SerializedName( "placeholder_model" )
	protected ModelPath placeholder_model_path;
	
	@Expose
	@SideOnly( Side.CLIENT )
	protected float placeholder_scale;
	
	@SideOnly( Side.CLIENT )
	protected Mesh placeholder_mesh;
	// >>> Render Setup End <<<
	
	
	/**
	 * A snapshot of the module with default setup installed. Every new
	 * ItemStack can be treated as a copy of this snapshot. This avoids the
	 * overhead of installing modules for a gun part. Lazy init.
	 */
	protected NBTTagCompound snapshot_nbt;
	
	
	public GunPartType()
	{
		FMUM.SIDE.runIfClient( () -> {
			this.texture = Texture.GREEN;
			this.models = new AnimatedModel[ 0 ];
			this.fp_scale = 1.0F;
			this.fp_pos = Vec3f.ORIGIN;
			this.modify_pos = new Vec3f( 0.0F, 0.0F, 150.0F / 160.0F );
			this.fp_rot = Quat4f.IDENTITY;
			this.mod_anim_channel = IAnimation.CHANNEL_NONE;
			
			this.placeholder_model_path = GunPartPlaceholder.MODEL_PATH;
			this.placeholder_scale = 1.0F;
		} );
	}
	
	
	@Override
	public void build( JsonObject data, String fallback_name, IContentBuildContext ctx )
	{
		super.build( data, fallback_name, ctx );
		
		// Validate member variable setup.
		if ( this.category == null ) {
			this.category = ItemCategory.parse( this.name );
		}
		if ( this.paintjobs.isEmpty() ) {
			this.paintjobs = new ArrayList<>();
		}
		this.paintjobs.add( 0, new IPaintjob() {
			@Override
			@SideOnly( Side.CLIENT )
			public Texture getTexture() {
				return GunPartType.this.texture;
			}
		} );
		
		// Try to assemble default setup once in post load to make sure it works.
		ctx.regisPostLoadCallback( c -> {
			// This is not necessary, but put it here makes sure that the user \
			// will get warning message early if any setup module is missing.
			if ( this.snapshot_nbt == null ) {
				this.snapshot_nbt = this._buildSnapshotNBT();
			}
		} );
		FMUM.SIDE.runIfClient( () -> {
			// Load mesh on client side.
			ctx.regisMeshLoadCallback( this::_loadMesh );
		} );
	}
	
	@Override
	protected Item _setupVanillaItem( IContentBuildContext ctx )
	{
		final ItemGunPart item = new ItemGunPart();
		item.setRegistryName( this.pack_info.getNamespace(), this.name );
		item.setTranslationKey( this.name );
		item.setMaxStackSize( 1 );
		
		MinecraftForge.EVENT_BUS.register( new Object() {
			@SubscribeEvent
			void _onItemRegis( Register< Item > evt ) {
				evt.getRegistry().register( item );
			}
			
			@SubscribeEvent
			@SideOnly( Side.CLIENT )
			void _onModelRegis( ModelRegistryEvent evt )
			{
				// TODO: Regis paintjob variants.
				final ResourceLocation res_loc = Objects.requireNonNull( item.getRegistryName() );
				final ModelResourceLocation model_res = new ModelResourceLocation( res_loc, "inventory" );
				ModelLoader.setCustomModelResourceLocation( item, 0, model_res );
			}
		} );
		
		return item;
	}
	
	protected NBTTagCompound _buildSnapshotNBT()
	{
		final IModule raw = this.createRawModule();
		final IModule built = this.default_setup.apply( raw, name -> {
			final Optional< IModuleType > mt = IModuleType.REGISTRY.lookup( name );
			if ( mt.isPresent() ) {
				return mt.map( IModuleType::createRawModule );
			}
			
			FMUM.LOGGER.error( "fmum.setup_module_not_found", this, name );
			return Optional.empty();
		} );
		return built.getBoundNBT();
	}
	
	@SideOnly( Side.CLIENT )
	protected void _loadMesh( IMeshLoadContext ctx )
	{
		Arrays.stream( this.models ).forEachOrdered(
			model -> model.mesh = ctx.loadMesh( model.model_path ).orElse( Mesh.NONE )
		);
		this.placeholder_mesh = ctx.loadMesh( this.placeholder_model_path ).orElse( Mesh.NONE );
	}
	
	@Override
	public ItemStack newItemStack( short meta )
	{
		// Lazy init snapshot nbt.
		// FIXME: Recheck this.
		if ( this.snapshot_nbt == null ) {
			this.snapshot_nbt = this._buildSnapshotNBT();
		}
		
		return new ItemStack( this.vanilla_item );
	}
	
	@Override
	public void injectPaintjob( IPaintjob paintjob ) {
		this.paintjobs.add( paintjob );
	}
	
	@Override
	public IModule createRawModule() {
		return new GunPart();
	}
	
	@Override
	public IModule takeAndDeserialize( NBTTagCompound nbt )
	{
		final GunPart self = new GunPart( nbt );
		self._deserializeAndBound( nbt );
		return self;
	}
	
	
	protected class ItemGunPart extends Item
	{
		protected static final String CAPABILITY_TAG = "~";
		
		protected ItemGunPart() { }
		
		@Override
		public ICapabilityProvider initCapabilities(
			@Nonnull ItemStack stack,
			@Nullable NBTTagCompound cap_nbt
		) {
			// 4 case to handle: \
			// has-stack_nbt & has-cap_nbt: {ItemStack#ItemStack(NBTTagCompound)} \
			// has-stack_nbt & no--cap_nbt: \
			// no--stack_nbt & has-cap_nbt: {ItemStack#copy()} \
			// no--stack_nbt & no--cap_nbt: {new ItemStack(...)}, {PacketBuffer#readItemStack()} \
			//
			// Case 1/3 -> CapabilityProvider#deserializeNBT(...) will be called.
			// Case 4 - PacketBuff#... -> See #getNBTShareTag(...) and #readNBTShareTag(...).
			if ( stack.getTagCompound() == null )
			{
				if ( cap_nbt != null )
				{
					// Because ItemStack#copy() will directly pass in the \
					// ICapabilityProvider#serializeNBT() result, we need to \
					// copy the tag to avoid changing the original NBT.
					final NBTBase mod_tag = cap_nbt.getTag( "Parent" );
					cap_nbt.setTag( "Parent", mod_tag.copy() );
				}
				else
				{
					// For {new ItemStack(...)} case, set a new NBT tag to avoid NPE.
					stack.setTagCompound( new NBTTagCompound() );
				}
			}
			return new CapabilityProvider();
		}
		
		@Override
		public NBTTagCompound getNBTShareTag( ItemStack stack )
		{
			// Copy to avoid changing the original NBT of the stack.
			final NBTTagCompound stack_nbt = Objects.requireNonNull( stack.getTagCompound() );
			final NBTTagCompound copied_nbt = stack_nbt.copy();
			
			final CapabilityProvider cap_provider = (
				IItem.ofOrEmpty( stack )
				.map( CapabilityProvider.class::cast )
				.orElseThrow( IllegalStateException::new )
			);
			copied_nbt.setTag( CAPABILITY_TAG, cap_provider.serializeNBT() );
			return copied_nbt;
		}
		
		@Override
		@SuppressWarnings( "DataFlowIssue" )
		public void readNBTShareTag( @Nonnull ItemStack stack, @Nullable NBTTagCompound nbt )
		{
			if ( nbt.hasKey( CAPABILITY_TAG, NBT.TAG_COMPOUND ) )
			{
				final NBTTagCompound cap_nbt = nbt.getCompoundTag( CAPABILITY_TAG );
				final CapabilityProvider cap_provider = (
					IItem.ofOrEmpty( stack )
					.map( CapabilityProvider.class::cast )
					.orElseThrow( IllegalStateException::new )
				);
				cap_provider.deserializeNBT( cap_nbt );
				nbt.removeTag( CAPABILITY_TAG );
			}
			super.readNBTShareTag( stack, nbt );
		}
		
		// Avoid breaking blocks when holding this item in survive mode.
		@Override
		public boolean onBlockStartBreak(
			@Nonnull ItemStack itemstack,
			@Nonnull BlockPos pos,
			@Nonnull EntityPlayer player
		) {
			return true;
		}
		
		// Avoid breaking blocks when holding this item in creative mode.
		@Override
		public boolean canDestroyBlockInCreative(
			@Nonnull World world,
			@Nonnull BlockPos pos,
			@Nonnull ItemStack stack,
			@Nonnull EntityPlayer player
		) {
			return false;
		}
	}
	
	
	public class CapabilityProvider implements ICapabilitySerializable< NBTTagCompound >, IItem
	{
		protected NBTTagCompound nbt = null;
		protected IModule gun_part = null;
		
		protected CapabilityProvider() { }
		
		@Override
		public boolean equals( Object obj )
		{
			if ( this == obj ) {
				return true;
			}
			
			final boolean is_gun_part = obj instanceof CapabilityProvider;
			if ( !is_gun_part ) {
				return false;
			}
			
			final CapabilityProvider cp = ( CapabilityProvider ) obj;
			return this.getType() == cp.getType();
		}
		
		@Override
		@SuppressWarnings("ConstantValue")
		public boolean hasCapability ( @Nonnull Capability < ? > capability, @Nullable EnumFacing facing ) {
			return capability == IItem.CAPABILITY;
		}
		
		@Nullable
		@Override
		@SuppressWarnings("ConstantValue")
		public <T > T getCapability( @Nonnull Capability < T > capability, @Nullable EnumFacing facing ) {
			return capability == IItem.CAPABILITY ? IItem.CAPABILITY.cast( this ) : null;
		}
		
		@Override
		public IItemType getType() {
			return GunPartType.this;
		}
		
		@Override
		@SuppressWarnings("DataFlowIssue")
		public < T > Optional< T > lookupCapability( Capability < T > capability )
		{
			if ( capability != IModule.CAPABILITY ) {
				return Optional.empty();
			}
			
			if ( this.gun_part == null )
			{
				if ( this.nbt == null || this.nbt.isEmpty() ) {
					this.nbt = GunPartType.this.snapshot_nbt.copy();
				}
				this.gun_part = IModule.takeAndDeserialize( this.nbt );
			}
			
			final T t = IModule.CAPABILITY.cast( this.gun_part );
			return Optional.of( t );
		}
		
		@Override
		public IEquippedItem onTakeOut( EnumHand hand, EntityPlayer player ) {
			return new EquippedGunPart();
		}
		
		@Override
		public NBTTagCompound serializeNBT ( ) {
			return this.nbt != null ? this.nbt : new NBTTagCompound();
		}
		
		@Override
		public void deserializeNBT ( NBTTagCompound nbt )
		{
			assert this.gun_part == null;
//			this.nbt = nbt.isEmpty() ? null : nbt;
			this.nbt = nbt;
		}
	}
	
	
	protected class GunPart extends Module implements IGunPart
	{
		protected short offset;
		protected short step;
		
		
		protected GunPart() { }
		
		protected GunPart( NBTTagCompound nbt ) {
			super( nbt );  // FIXME
		}
		
		@Override
		protected void _deserializeAndBound( NBTTagCompound nbt )
		{
			super._deserializeAndBound( nbt );
			
			final int[] data = nbt.getIntArray( DATA_TAG );
			final int value = data[ super._getDataArrLen() ];
			this.offset = ( short ) value;
			this.step = ( short ) ( value >>> 16 );
		}
		
		@Override
		public ItemCategory getCategory() {
			return GunPartType.this.category;
		}
		
		@Override
		public Optional< ? extends IGunPart > getBase() {
			return super.getBase().map( IGunPart.class::cast );
		}
		
		@Override
		public IGunPart getInstalled( int slot_idx, int module_idx ) {
			return ( IGunPart ) super.getInstalled( slot_idx, module_idx );
		}
		
		@Override
		public int getPaintjobCount() {
			return GunPartType.this.paintjobs.size();
		}
		
		@Override
		public int getSlotCount() {
			return GunPartType.this.slots.length;
		}
		
		@Override
		public int getOffsetCount() {
			return GunPartType.this.offsets.length;
		}
		
		@Override
		public int getOffset() {
			return this.offset;
		}
		
		@Override
		public int getStep() {
			return this.step;
		}
		
		@Override
		public int getStepCount( int slot_idx )
		{
			final RailSlot slot = GunPartType.this.slots[ slot_idx ];
			return slot.max_step + 1;
		}
		
		@Override
		public IModifyPreview< Pair< Integer, Integer > > trySetOffsetAndStep( int offset, int step ) {
			return IModifyPreview.of( () -> this._setOffsetAndStep( offset, step ) );
		}
		
		protected Pair< Integer, Integer > _setOffsetAndStep( int offset, int step )
		{
			this.offset = ( short ) offset;
			this.step = ( short ) step;
			final int[] data = this.nbt.getIntArray( DATA_TAG );
			data[ super._getDataArrLen() ] = ( step << 16 ) | ( 0xFFFF & offset );
			return Pair.of( offset, step );
		}
		
		@Override
		public IModifyPreview< Integer > tryInstall( int slot_idx, IModule module )
		{
			final Supplier< Integer > action = () -> this._install( slot_idx, module );
			final RailSlot slot = GunPartType.this.slots[ slot_idx ];
			if ( !slot.category_predicate.test( module.getCategory() ) ) {
				return IModifyPreview.ofPreviewError( action, "Incompatible module" );
			}
			
			final int capacity = Math.min( SyncConfig.max_slot_capacity, slot.capacity );
			if ( GunPart.this.countModuleInSlot( slot_idx ) >= capacity ) {
				return IModifyPreview.ofPreviewError( action, "Exceed max module capacity" );
			}
			
			// TODO: Check layer limitation
			return IModifyPreview.of( action );
		}
		
		@Override
		@SideOnly( Side.CLIENT )
		public Pair< ? extends IModule, Supplier< ? extends IModule > > getModifyCursor(
			int slot_idx,
			int module_idx,
			IModifyContext ctx
		) {
			// TODO: Do we need a method to turn it back to a normal module?
			final IGunPart module = this.getInstalled( slot_idx, module_idx );
			final IModule proxy = module.IGunPart$createSelectionProxy( ctx );
			
			// Replace installed module with its proxy.
			final NBTTagList mod_lst = this.nbt.getTagList( MODULE_TAG, NBT.TAG_COMPOUND );
			final int idx = this._getSlotStartIdx( slot_idx ) + module_idx;
			this.installed_modules.set( idx, proxy );
			mod_lst.set( idx, proxy.getBoundNBT() );
			proxy.IModule$setBase( this );
			
			return Pair.of( proxy, () -> {
				this.installed_modules.set( idx, module );
				mod_lst.set( idx, module.getBoundNBT() );
				return module;
			} );
		}
		
		@Override
		@SideOnly( Side.CLIENT )
		public int installPreviewPlaceholder( int slot_idx, IModifyContext ctx )
		{
			final RailSlot slot = GunPartType.this.slots[ slot_idx ];
			final float offset = 0.5F * slot.step_len * slot.max_step;
			
			final IGunPart placeholder = new GunPartPlaceholder()
			{
				@Override
				protected void _renderModel( IAnimator animator, IPoseSetup setup )
				{
					GL11.glPushMatrix();
					setup.glApply();
					GL11.glTranslatef( 0.0F, 0.0F, offset );
					GLUtil.glScale1f( GunPartType.this.placeholder_scale );
					GLUtil.bindTexture( Texture.GREEN );
					GLUtil.maxGlowOn();
					GunPartType.this.placeholder_mesh.draw();
					GLUtil.glowOff();
					GL11.glPopMatrix();
				}
			};
			return this._install( slot_idx, placeholder );
		}
		
		@Override
		@SideOnly( Side.CLIENT )
		public IModule IGunPart$createSelectionProxy( IModifyContext ctx )
		{
			final GunPart proxy = new GunPart() {
				@Override
				protected void _renderModel( IAnimator animator )
				{
					GL11.glPushMatrix();
					GLUtil.glMultMatrix( this.mat );
					GLUtil.glScale1f( GunPartType.this.fp_scale );
					
					// Highlight in green/red to indicate selection.
					final Texture tex = ctx.mapTexture( this._getTexture() );
					GLUtil.bindTexture( tex );
					GLUtil.maxGlowOn();
					Arrays.stream( GunPartType.this.models ).forEachOrdered( model -> model.render( animator ) );
					GLUtil.glowOff();
					
					GL11.glPopMatrix();
				}
			};
			proxy._deserializeAndBound( this.nbt.copy() );
			return proxy;
		}
		
		@Override
		@SideOnly( Side.CLIENT )
		public void IGunPart$prepareRender(
			int base_slot_idx,
			IAnimator animator,
			Collection< IRenderCallback > render_queue
		) {
			// Obtain render transform from base module.
			final Optional< ? extends IGunPart > base = this.getBase();
			if ( base.isPresent() ) {
				base.get().IGunPart$getRenderSetup( this, base_slot_idx ).getTransform( this.mat );
			}
			else {
				animator.getChannel( IEquippedItem.CHANNEL_ITEM ).getTransform( this.mat );
			}
			
			// Apply animation specific to this gun part.
			animator.getChannel( GunPartType.this.mod_anim_channel ).applyTransform( this.mat );
			
			// Enqueue render callback.
			render_queue.add( () -> this._renderModel( animator ) );
			
			// Dispatch to child modules.
			final ListIterator< IModule > itr = this.installed_modules.listIterator();
			final int slt_cnt = this.split_indices.length;
			for ( int slt_idx = 0; slt_idx < slt_cnt; slt_idx += 1 )
			{
				final int end = this._getSlotStartIdx( slt_idx + 1 );
				while ( itr.nextIndex() < end )
				{
					final IGunPart child = ( IGunPart ) itr.next();
					child.IGunPart$prepareRender( slt_idx, animator, render_queue );
				}
			}
		}
		
		@Override
		public ItemStack takeAndToStack()
		{
			final ItemStack stack = GunPartType.this.newItemStack( ( short ) 0 );
			final CapabilityProvider cap_provider = (
				IItem.ofOrEmpty( stack )
				.map( CapabilityProvider.class::cast )
				.orElseThrow( IllegalStateException::new )
			);
			cap_provider.deserializeNBT( this.nbt.copy() );
			return stack;
		}
		
		@SideOnly( Side.CLIENT )
		protected void _renderModel( IAnimator animator )
		{
			GL11.glPushMatrix();
			GLUtil.glMultMatrix( this.mat );
			GLUtil.glScale1f( GunPartType.this.fp_scale );
			GLUtil.bindTexture( this._getTexture() );
			Arrays.stream( GunPartType.this.models ).forEachOrdered( model -> model.render( animator ) );
			GL11.glPopMatrix();
		}
		
		@SideOnly( Side.CLIENT )
		protected Texture _getTexture() {
			return GunPartType.this.paintjobs.get( this.paintjob_idx ).getTexture();
		}
		
		@Override
		@SideOnly( Side.CLIENT )
		public IPoseSetup IGunPart$getRenderSetup( IGunPart gun_part, int slot_idx )
		{
			final Mat4f dst = new Mat4f();
			dst.set( this.mat );
			
			final RailSlot slot = GunPartType.this.slots[ slot_idx ];
			final float step_offset = slot.step_len * gun_part.getStep();
			
			final Vec3f origin = slot.origin;
			dst.translate( origin.x, origin.y, origin.z + step_offset );
			dst.rotateZ( slot.rot_z );
			return IPoseSetup.of( dst );
		}
		
		@Override
		public String toString() {
			return String.format( "Item<%s>", GunPartType.this );
		}
		
		@Override
		protected short _getModuleID()
		{
			final Optional< Short > id = IModuleType.REGISTRY.lookupID( GunPartType.this );
			assert id.isPresent();
			return id.get();
		}
		
		@Override
		protected int _getDataArrLen() {
			return 1 + super._getDataArrLen();
		}
	}
}
