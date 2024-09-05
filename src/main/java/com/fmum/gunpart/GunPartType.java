package com.fmum.gunpart;

import com.fmum.FMUM;
import com.fmum.SyncConfig;
import com.fmum.input.IInput;
import com.fmum.input.Inputs;
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
import com.fmum.module.ModifyTracker;
import com.fmum.module.Module;
import com.fmum.paintjob.IPaintableType;
import com.fmum.paintjob.IPaintjob;
import com.fmum.player.PlayerPatchClient;
import com.fmum.render.AnimatedModel;
import com.fmum.render.IAnimator;
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
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.IntStream;

public class GunPartType extends ItemType implements IModuleType, IPaintableType
{
	protected static final String STACK_ID_TAG = "!";
	
	
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
		this.paintjobs.add( 0, () -> this.texture );
		
		// Try to assemble default setup once in post load to make sure it works.
		ctx.regisPostLoadCallback( _ctx -> {
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
	@SuppressWarnings( "DataFlowIssue" )
	public ItemStack newItemStack( short meta )
	{
		// Lazy init snapshot nbt.
		if ( this.snapshot_nbt == null ) {
			this.snapshot_nbt = this._buildSnapshotNBT();
		}
		
		final ItemStack stack = new ItemStack( ItemGunPart.INSTANCE );
		final GunPartCapProvider provider = stack.getCapability( GunPartCapProvider.CAPABILITY, null );
		final NBTTagCompound its_nbt_tag = this.snapshot_nbt.copy();
		provider.deserializeNBT( its_nbt_tag );
		return stack;
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
	
	
	protected abstract class GunPartItem implements IItem
	{
		@Override
		public IItemType getType() {
			return GunPartType.this;
		}
		
		@Override
		public boolean equals( Object obj )
		{
			if ( this == obj ) {
				return true;
			}
			
			final boolean is_gun_part = obj instanceof GunPartItem;
			if ( !is_gun_part ) {
				return false;
			}
			
			final GunPartItem gp = ( GunPartItem ) obj;
			return this._getStackID() == gp._getStackID();
		}
		
		protected int _getStackID()
		{
			final NBTTagCompound nbt = this.getBoundStack().getTagCompound();
			return Objects.requireNonNull( nbt ).getInteger( STACK_ID_TAG );
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
			if ( GunPart.this.countModuleInSlot( slot_idx ) > capacity ) {
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
		@SuppressWarnings( "DataFlowIssue" )
		public ItemStack takeAndToStack()
		{
			final ItemStack stack = new ItemStack( ItemGunPart.INSTANCE );
			final GunPartCapProvider provider = stack.getCapability( GunPartCapProvider.CAPABILITY, null );
			provider.deserializeNBT( this.getBoundNBT() );
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
		
		protected IItem _createItem( ItemStack stack )
		{
			return new GunPartItem() {
				@Override
				public ItemStack getBoundStack() {
					return stack;
				}
				
				@Override
				@SuppressWarnings( { "DataFlowIssue" } )
				public < U > Optional< U > lookupCapability( Capability< U > capability )
				{
					if ( capability != IModule.CAPABILITY ) {
						return Optional.empty();
					}
					
					final U cast = IModule.CAPABILITY.cast( GunPart.this );
					return Optional.of( cast );
				}
				
				@Override
				public IEquippedItem onTakeOut( EnumHand hand, EntityPlayer player ) {
					return new EquippedGunPart();
				}
			};
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
	
	
	protected static class EquippedGunPart implements IEquippedItem
	{
		protected ModifyTracker operation;
		
		@SideOnly( Side.CLIENT )
		protected ArrayList< IRenderCallback > in_hand_queue;
		
		protected EquippedGunPart() {
			FMUM.SIDE.runIfClient( () -> this.in_hand_queue = new ArrayList<>() );
		}
		
		@Override
		public IEquippedItem tickInHand( EnumHand hand, IItem held_item, EntityPlayer player )
		{
			return this;
		}
		
		@Override
		@SideOnly( Side.CLIENT )
		public IEquippedItem onInputUpdate( String name, IInput input, IItem item )
		{
			final boolean is_activation = input.getAsBool();
			if ( !is_activation ) {
				return this;
			}
			
			switch ( name )
			{
			case Inputs.OPEN_MODIFY_VIEW:
				return new EquippedModifying( this, item );
			}
			
			return this;
		}
		
		@Override
		@SideOnly( Side.CLIENT )
		public void prepareRenderInHand( EnumHand hand, IItem item )
		{
			final IGunPart self = this._getRenderDelegate( item );
			
			// Clear previous in hand queue.
			this.in_hand_queue.clear();
			
			// Collect render callback.
			final IAnimator animator = this._getAnimator( item );  // TODO: Replace with a real animator.
			self.IGunPart$prepareRender( -1, animator, this.in_hand_queue );
			
			// Sort render callback based on priority.
			// TODO: Reverse or not?
			this.in_hand_queue.sort( Comparator.comparing( rc -> rc.getPriority( IPoseSetup.EMPTY ) ) );
		}
		
		@SideOnly( Side.CLIENT )
		protected IAnimator _getAnimator( IItem item ) {
			return IAnimator.NONE;  // TODO: Replace with a real animator.
		}
		
		@Override
		@SideOnly( Side.CLIENT )
		public boolean renderInHand( EnumHand hand, IItem item )
		{
			GL11.glPushMatrix();
			
			// Apply customized rendering.
			final Minecraft mc = Minecraft.getMinecraft();
			final EntityPlayerSP player = mc.player;
			
			// Copied from {@link EntityRenderer#renderHand(float, int)}.
			final EntityRenderer renderer = mc.entityRenderer;
			renderer.enableLightmap();
			
			// Copied from {@link ItemRenderer#renderItemInFirstPerson(float)}.
			// {@link ItemRenderer#rotateArroundXAndY(float, float)}.
			PlayerPatchClient.get().camera.getCameraSetup().glApply();
			
			GLUtil.glRotateYf( 180.0F );
			RenderHelper.enableStandardItemLighting();
			
			// {@link ItemRenderer#setLightmap()}.
			final double eye_pos_y = player.posY + player.getEyeHeight();
			final BlockPos block_pos = new BlockPos( player.posX, eye_pos_y, player.posZ );
			final int light = mc.world.getCombinedLight( block_pos, 0 );
			
			final float x = light & 0xFFFF;
			final float y = light >> 16;
			OpenGlHelper.setLightmapTextureCoords( OpenGlHelper.lightmapTexUnit, x, y );
			
			// {@link ItemRenderer#rotateArm(float)} is left out to avoid shift.
			
			// TODO: Re-scale may not be needed. Do not forget that there is a disable pair call.
			GlStateManager.enableRescaleNormal();
			
			// Setup and render!
			GLUtil.glRotateYf( 180.0F - player.rotationYaw );
			GLUtil.glRotateXf( player.rotationPitch );
			this._doRenderInHand( hand, item );
			
			GlStateManager.disableRescaleNormal();
			RenderHelper.disableStandardItemLighting();
			// End of {@link ItemRenderer#renderItemInFirstPerson(float)}.
			
			renderer.disableLightmap();
			
			GL11.glPopMatrix();
			return true;
		}
		
		/**
		 * No need to push matrix here as caller should have done it.
		 */
		@SideOnly( Side.CLIENT )
		protected void _doRenderInHand( EnumHand hand, IItem item )
		{
//			Dev.cur().applyTransRot();
			this.in_hand_queue.forEach( IRenderCallback::render );
		}
		
		@Override
		@SideOnly( Side.CLIENT )
		public boolean renderSpecificInHand( EnumHand hand, IItem item ) {
			return true;
		}
		
		@SideOnly( Side.CLIENT )
		protected IGunPart _getRenderDelegate( IItem item )
		{
			final Optional< IModule > opt = item.lookupCapability( IModule.CAPABILITY );
			return ( IGunPart ) opt.orElseThrow( IllegalArgumentException::new );
		}
	}
	
	
	@SideOnly( Side.CLIENT )
	protected static class EquippedModifying extends EquippedGunPart
	{
		protected final IEquippedItem wrapped;
		protected final ModifyTracker tracker;
		
		protected IItem item;
		
		protected EquippedModifying( IEquippedItem wrapped, IItem item )
		{
			this.wrapped = wrapped;
			this.tracker = new GunPartModifyTracker( this._newFactory( item ) );
			this.item = item;
		}
		
		protected Supplier< ? extends IModule > _newFactory( IItem item )
		{
			final IGunPart self = super._getRenderDelegate( item );
			final NBTTagCompound nbt = self.getBoundNBT();
			return () -> IModule.takeAndDeserialize( nbt.copy() );
		}
		
		@Override
		protected IGunPart _getRenderDelegate( IItem item ) {
			return ( IGunPart ) this.tracker.getRoot();
		}
		
		@Override
		public IEquippedItem tickInHand( EnumHand hand, IItem held_item, EntityPlayer player )
		{
			if ( held_item != this.item )
			{
				this.tracker.refresh( this._newFactory( held_item ), i -> {
					final InventoryPlayer inv = Minecraft.getMinecraft().player.inventory;
					return (
						IItem.ofOrEmpty( inv.getStackInSlot( i ) )
						.flatMap( it -> it.lookupCapability( IModule.CAPABILITY ) )
						// Copy to avoid side effect.
						.map( IModule::getBoundNBT )
						.map( NBTTagCompound::copy )
						.map( IModule::takeAndDeserialize )
					);
				} );
				this.item = held_item;
			}
			return this;
		}
		
		@Override
		public IEquippedItem onInputUpdate( String name, IInput input, IItem item )
		{
			final boolean is_activation = input.getAsBool();
			if ( !is_activation ) {
				return this;
			}
			
			switch ( name )
			{
			case Inputs.OPEN_MODIFY_VIEW:
				return this.wrapped;
			
			case Inputs.NEXT_MODIFY_MODE:
				this.tracker.loopModifyMode();
				break;
			case Inputs.ENTER_LAYER:
				this.tracker.enterLayer();
				break;
			case Inputs.QUIT_LAYER:
				this.tracker.quitLayer( this._newFactory( item ) );
				break;
			case Inputs.LAST_SLOT:
			case Inputs.NEXT_SLOT:
				this.tracker.loopSlot( name.equals( Inputs.NEXT_SLOT ) );
				break;
			case Inputs.LAST_MODULE:
			case Inputs.NEXT_MODULE:
				this.tracker.loopModule( name.equals( Inputs.NEXT_MODULE ) );
				break;
			case Inputs.LAST_PREVIEW:
			case Inputs.NEXT_PREVIEW:
				this.tracker.loopPreview( prev_idx -> {
					final InventoryPlayer inv = Minecraft.getMinecraft().player.inventory;
					final int size = inv.getSizeInventory();
					final IntStream inv_indices = (
						name.equals( Inputs.NEXT_PREVIEW )
						? IntStream.range( prev_idx + 1, size )
						: IntStream.range( size - ( prev_idx + size + 1 ) % ( size + 1 ), size )
							.map( i -> size - i - 1 )
					);
					return (
						inv_indices
						.mapToObj( i -> (
							IItem.ofOrEmpty( inv.getStackInSlot( i ) )
							.filter( it -> !it.equals( item ) )
							.flatMap( it -> it.lookupCapability( IModule.CAPABILITY ) )
							// Copy to avoid side effect.
							.map( IModule::getBoundNBT )
							.map( NBTTagCompound::copy )
							.map( IModule::takeAndDeserialize )
							.map( mod -> Pair.of( i, mod ) )
						) )
						.filter( Optional::isPresent )
						.map( Optional::get )
						.iterator()
					);
				} );
				break;
			case Inputs.LAST_CHANGE:
			case Inputs.NEXT_CHANGE:
				this.tracker.loopChange( name.equals( Inputs.NEXT_CHANGE ) );
				break;
			case Inputs.CONFIRM_CHANGE:
				this.tracker.confirmChange();
				break;
			case Inputs.REMOVE_MODULE:
				this.tracker.removeModule();
				break;
			}
			return this;
		}
		
		@Override
		protected IAnimator _getAnimator( IItem item )
		{
			final EntityPlayerSP player = Minecraft.getMinecraft().player;
			final GunPartType type = ( GunPartType ) item.getType();
			final Vec3f modify_pos = type.modify_pos;
			final Mat4f mat = new Mat4f();
			mat.setIdentity();
			mat.translate( 0.0F, 0.0F, modify_pos.z );
			mat.rotateX( -player.rotationPitch );
			mat.rotateY( 90.0F + player.rotationYaw );
			mat.translate( modify_pos.x, modify_pos.y, 0.0F );
			final IPoseSetup in_hand_setup = IPoseSetup.of( mat );
			return channel -> channel.equals( CHANNEL_ITEM ) ? in_hand_setup : IPoseSetup.EMPTY;
		}
	}
}
