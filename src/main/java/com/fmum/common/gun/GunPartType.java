package com.fmum.common.gun;

import static com.fmum.common.gun.GunPartWrapper.STACK_ID_TAG;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.function.Function;

import com.fmum.client.FMUMClient;
import com.fmum.client.gun.IEquippedGunPartRenderer;
import com.fmum.client.gun.IGunPartRenderer;
import com.fmum.client.input.IInput;
import com.fmum.client.input.Key;
import com.fmum.client.item.IItemModel;
import com.fmum.client.module.IDeferredRenderer;
import com.fmum.client.player.OpModifyClient;
import com.fmum.client.player.PlayerPatchClient;
import com.fmum.client.render.IAnimator;
import com.fmum.common.FMUM;
import com.fmum.common.item.IEquippedItem;
import com.fmum.common.item.IItemTypeHost;
import com.fmum.common.item.ItemType;
import com.fmum.common.load.IContentProvider;
import com.fmum.common.meta.IMeta;
import com.fmum.common.module.IModule;
import com.fmum.common.module.IModuleSlot;
import com.fmum.common.module.IModuleType;
import com.fmum.common.module.Module;
import com.fmum.common.module.ModuleCategory;
import com.fmum.common.module.ModuleSnapshot;
import com.fmum.common.paintjob.IPaintableType;
import com.fmum.common.paintjob.IPaintjob;
import com.fmum.common.player.IOperation;
import com.fmum.devtool.Dev;
import com.fmum.util.ArmTracker;
import com.fmum.util.Mat4f;
import com.google.gson.annotations.SerializedName;

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

import javax.annotation.Nonnull;

public abstract class GunPartType<
	I extends IGunPart< ? extends I >, // Not necessary, but to avoid filling in the generic argument on instantiating the abstract inner class.
	C extends IGunPart< ? >,
	E extends IEquippedItem< ? extends C >,
	ER extends IEquippedGunPartRenderer< ? super E >,
	R extends IGunPartRenderer< ? super C, ? extends ER >,
	M extends IItemModel< ? extends R >
> extends ItemType< C, M > implements IModuleType, IPaintableType, IPaintjob
{
	protected static final float[] OFFSETS = { 0F };
	
	@SerializedName( value = "category", alternate = "group" )
	protected ModuleCategory category;
	
	@SerializedName( value = "paramScale", alternate = "scale" )
	protected float paramScale = 1F;
	
	@SerializedName( value = "slots", alternate = "moduleSlots" )
	protected List< IModuleSlot > slots = Collections.emptyList();
	
	@SerializedName( value = "snapshot", alternate = "preInstalls" )
	protected ModuleSnapshot snapshot = ModuleSnapshot.DEFAULT;
	protected transient NBTTagCompound compiledSnapshotNBT;
	
	@SerializedName( value = "offsets", alternate = "installOffsets" )
	protected float[] offsets = OFFSETS;
	
	@SerializedName( value = "paintjobs", alternate = "skins" )
	protected List< IPaintjob > paintjobs = Collections.emptyList();
	
	protected int leftHandPriority = Integer.MIN_VALUE; // TODO: move to grip type maybe?
	protected int rightHandPriority = Integer.MIN_VALUE;
	
	@SideOnly( Side.CLIENT )
	protected String modifyIndicator;
	
	@Override
	public IMeta build( String name, IContentProvider provider )
	{
		super.build( name, provider );
		
		IModuleType.REGISTRY.regis( this );
		IPaintableType.REGISTRY.regis( this );
		
		// If not category set then set it is its name.
		if ( this.category == null ) { this.category = new ModuleCategory( this.name ); }
		provider.clientOnly( () -> {
			if ( this.modifyIndicator == null ) {
				this.modifyIndicator = FMUMClient.MODIFY_INDICATOR;
			}
		} );
		
		// Add itself as the default paintjob.
		if ( this.paintjobs.size() == 0 ) { this.paintjobs = new ArrayList<>(); }
		this.paintjobs.add( 0, this );
		
		// Apply model scale.
		this.slots.forEach( slot -> slot.scale( this.paramScale ) );
		// TODO: hitboxes
		return this;
	}
	
	@Override
	public void onPostLoad()
	{
		super.onPostLoad();
		
		this.provider.clientOnly( () -> {
			if ( IModuleType.REGISTRY.get( this.modifyIndicator ) == null )
			{
				this.logError( "fmum.fail_to_find_indicator", this, this.modifyIndicator );
				this.modifyIndicator = FMUMClient.MODIFY_INDICATOR;
			}
		} );
		
		// TODO: call update state maybe?
		this.snapshot.setSnapshot(
			name -> this.newRawContexted(),
			module -> this.compiledSnapshotNBT = module.serializeNBT()
		);
		this.snapshot = null; // Release snapshot after use.
	}
	
	@Override
	public void injectPaintjob( IPaintjob paintjob ) { this.paintjobs.add( paintjob ); }
	
	@Override
	@SideOnly( Side.CLIENT )
	public ResourceLocation texture() { return this.texture; }
	
	// TODO: handle paintjobs
	@Override
	protected Item createItem() { return new GunPartVanillaItem( 1, 0 ); }
	
	protected final IModule< ? > fromTag( NBTTagCompound tag )
	{
		final Item item = Item.getItemById( IModule.getId( tag ) );
		final IModuleType type = ( IModuleType ) IItemTypeHost.getType( item );
		return type.deserializeContexted( tag );
	}
	
	protected abstract ICapabilityProvider newWrapper( C primary, ItemStack stack );
	
	protected class GunPartVanillaItem extends VanillaItem
	{
		protected GunPartVanillaItem( int maxStackSize, int maxDamage ) {
			super( maxStackSize, maxDamage );
		}
		
		@Override
		@SuppressWarnings( "unchecked" )
		public ICapabilityProvider initCapabilities( ItemStack stack, NBTTagCompound capTag )
		{
			final Function< C, ICapabilityProvider > finializer = primary -> {
				final ICapabilityProvider wrapper = GunPartType.this.newWrapper( primary, stack );
				primary.syncAndUpdate();
				return wrapper;
			};
			
			// 4 case to handle: \
			// has-stackTag | has-capTag}: {ItemStack#ItemStack(NBTTagCompound)} \
			// has-stackTag | no--capTag}: \
			// no--stackTag | has-capTag}: {ItemStack#copy()} \
			// no--stackTag | no--capTag}: {new ItemStack(...)}, {PacketBuffer#readItemStack()} \
			final NBTTagCompound stackTag = stack.getTagCompound();
			if ( capTag != null )
			{
				// 2 cases possible:
				// no--stackTag | has-capTag: {ItemStack#copy()}.
				// has-stackTag | has-capTag: {ItemStack#ItemStack(NBTTagCompound)}.
				final NBTTagCompound nbt = capTag.getCompoundTag( "Parent" );
				
				// Remove "Parent" tag to prevent repeat deserialization.
				// See CapabilityDispatcher#deserializeNBT(NBTTagCompound).
				capTag.removeTag( "Parent" );
				
				NBTTagCompound primaryTag;
				if ( stackTag == null )
				{
					// Has to copy before use if it is the first case as the capability tag \
					// provided here could be the same as the bounden tag of copy target.
					primaryTag = nbt.copy();
					
					// To ensure #syncAndUpdate() call will not crash on null.
					stack.setTagCompound( new NBTTagCompound() ); // TODO: static instance
				}
				else primaryTag = nbt;
				
				return finializer.apply( ( C ) GunPartType.this.fromTag( primaryTag ) );
			}
			
			if ( stackTag != null )
			{
				// has-stackTag | no--capTag: should never happen.
				Dev.dirtyMark();
				throw new RuntimeException( "has-stackTag | no--capTag: should never happen" );
			}
			
			// no--stackTag | no--capTag: {new ItemStack(...)}, {PacketBuffer#readItemStack()}
			// We basically have no way to distinguish from these two cases. But it will work \
			// fine if we simply deserialize and setup it with the compiled snapshot NBT. That \
			// is because #readNBTShareTag(ItemStack, NBTTagCompound) will later be called for \
			// the network packet case. The downside is that it will actually deserialize \
			// twice for the network packet case.
			final NBTTagCompound newStackTag = new NBTTagCompound();
			newStackTag.setInteger( STACK_ID_TAG, new Random().nextInt() ); // TODO: better way to do this?
			stack.setTagCompound( newStackTag );
			// See GunPartWrapper#stackId().
			
			final NBTTagCompound primaryTag = GunPartType.this.compiledSnapshotNBT.copy();
			return finializer.apply( ( C ) GunPartType.this.deserializeContexted( primaryTag ) );
		}
		
		@Override
		public void readNBTShareTag( ItemStack stack, NBTTagCompound nbt )
		{
			stack.setTagCompound( nbt ); // Copied from super.
			
			// See GunPartWrapper#syncNBTData().
			final NBTTagCompound primaryTag = nbt.getCompoundTag( "_" );
			final IModule< ? > primary = GunPartType.this.fromTag( primaryTag );
			
			final C wrapper = GunPartType.this.getContexted( stack );
			wrapper.setBase( primary, -1 ); // See GunPartWrapper#setBase(...).
			wrapper.syncAndUpdate();
		}
		
		/**
		 * <p> {@inheritDoc} </p>
		 * 
		 * <p> In default avoid to break the block when holding a this item in survive mode. </p>
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
	
	protected abstract class GunPart extends Module< I > implements IGunPart< I >
	{
		protected short offset;
		protected short step;
		
		@SideOnly( Side.CLIENT )
		protected transient R renderer;
		
		protected GunPart() {
			FMUM.MOD.clientOnly( () -> this.renderer = GunPartType.this.model.newRenderer() );
		}
		
		protected GunPart( boolean UNUSED_waitForDeserialize )
		{
			super( UNUSED_waitForDeserialize );
			
			// TODO: This will create renderer on local server
			// TODO: maybe provide more information on instantiation
			FMUM.MOD.clientOnly( () -> this.renderer = GunPartType.this.model.newRenderer() );
		}
		
		@Override
		public IEquippedItem< ? > onTakeOut( EntityPlayer player, EnumHand hand )
		{
			final NBTTagCompound stackTag = this.base.toStack().getTagCompound();
			final int stackId = stackTag.getInteger( STACK_ID_TAG );
			
			final boolean isLogicServer = !player.world.isRemote;
			if ( isLogicServer )
			{
				// This is required because we have no way to assign a new id for a copied gun \
				// stack without using the mixin, which means two guns in player's inventory \
				// could have essentially identical id! This hack generates a new id for gun in \
				// hand on taking out, which can not fully settle this problem, but should be \
				// enough to make it work in most of the time.
				
				// Only update id on server side.
				final int seed = stackId + player.inventory.currentItem;
				final int newStackId = new Random( seed ).nextInt();
				stackTag.setInteger( STACK_ID_TAG, newStackId );
			}
			else
			{
				// If id changed to the new id updated by server side, then just ignore.
				final IEquippedItem< ? > prev = PlayerPatchClient.instance.getEquipped( hand );
				final int prevStackId = prev.item().stackId();
				final int seed = prevStackId + player.inventory.currentItem;
				final int updatedStackId = new Random( seed ).nextInt();
				final boolean shouldIgnore = stackId == updatedStackId;
				if ( shouldIgnore ) { return this.onStackUpdate( prev, player, hand ); }
			}
			
			return this.newEquipped( player, hand );
		}
		
		@Override
		public IEquippedItem< ? > onStackUpdate(
			IEquippedItem< ? > prevEquipped,
			EntityPlayer player,
			EnumHand hand
		) { return this.copyEquipped( prevEquipped, player, hand ); }
		
		@Override
		public int stackId() { throw new RuntimeException(); }
		
		@Override
		public String name() { return GunPartType.this.name; }
		
		@Override
		public ModuleCategory category() { return GunPartType.this.category; }
		
		@Override
		public int paintjobCount() { return GunPartType.this.paintjobs.size(); }
		
		@Override
		public int slotCount() { return GunPartType.this.slots.size(); }
		
		@Override
		public IModuleSlot getSlot( int idx ) { return GunPartType.this.slots.get( idx ); }
		
		@Override
		public int offsetCount() { return GunPartType.this.offsets.length; }
		
		@Override
		public int offset() { return this.offset; }
		
		@Override
		public int step() { return this.step; }
		
		@Override
		public void setOffsetStep( int offset, int step )
		{
			this.offset = ( short ) offset;
			this.step = ( short ) step;
			final int[] data = this.nbt.getIntArray( DATA_TAG );
			data[ super.dataSize() ] = 0xFFFF & offset | step << 16;
			this.syncAndUpdate();
		}
		
		@Override
		public int leftHandPriority() { return GunPartType.this.leftHandPriority; }
		
		@Override
		public int rightHandPriority() { return GunPartType.this.rightHandPriority; }
		
		@Override
		public void getTransform( IModule< ? > installed, Mat4f dst )
		{
			dst.set( this.mat );
			
			final IModuleSlot slot = GunPartType.this.slots.get( installed.baseSlot() );
			slot.applyTransform( installed, dst );
		}
		
		@Override
		@SideOnly( Side.CLIENT )
		public void getRenderTransform( IModule< ? > installed, IAnimator animator, Mat4f dst )
		{
			this.renderer.getTransform( dst );
			
			final IModuleSlot slot = GunPartType.this.slots.get( installed.baseSlot() );
			slot.applyTransform( installed, dst );
		}
		
		@Override
		@SideOnly( Side.CLIENT )
		public void prepareRenderInHandSP(
			IAnimator animator,
			Collection< IDeferredRenderer > renderQueue0,
			Collection< IDeferredRenderer > renderQueue1
		) {
			this.renderer.prepareInHandRender( this.self(), animator, renderQueue0, renderQueue1 );
			
			this.installed.forEach(
				mod -> mod.prepareRenderInHandSP( animator, renderQueue0, renderQueue1 )
			);
		}
		
		@Override
		@SideOnly( Side.CLIENT )
		public void setupLeftArmToRender( IAnimator animator, ArmTracker leftArm ) {
			this.renderer.setupLeftArmToRender( animator, leftArm );
		}
		
		@Override
		@SideOnly( Side.CLIENT )
		public void setupRightArmToRender( IAnimator animator, ArmTracker rightArm ) {
			this.renderer.setupRightArmToRender( animator, rightArm );
		}
		
		@Override
		@SideOnly( Side.CLIENT )
		public ResourceLocation texture() {
			return GunPartType.this.paintjobs.get( this.paintjob ).texture();
		}
		
		@Override
		@SideOnly( Side.CLIENT )
		public IModule< ? > newModifyIndicator()
		{
			// TODO: obtain from item as new raw context will not create #renderer
			// TODO: maybe a buffered instance
			return IModuleType.REGISTRY.get( GunPartType.this.modifyIndicator ).newRawContexted();
		}
		
		@Override
		public void deserializeNBT( NBTTagCompound nbt )
		{
			super.deserializeNBT( nbt );
			
			final int[] data = nbt.getIntArray( DATA_TAG );
			final int value = data[ super.dataSize() ];
			this.offset = ( short ) value;
			this.step = ( short ) ( value >>> 16 );
		}
		
		@Override
		public String toString() { return "Contexted<" + GunPartType.this + ">"; }
		
		@Override
		protected int dataSize() { return super.dataSize() + 1; }
		
		@Override
		protected int id() { return Item.getIdFromItem( GunPartType.this.item ); }
		
		@Override
		protected final IModule< ? > fromTag( NBTTagCompound tag ) {
			return GunPartType.this.fromTag( tag );
		}
		
		// Maybe a bit of too hacky...
		@Override
		protected IModule< ? > wrapOnBeingRemoved()
		{
			final ItemStack stack = new ItemStack( GunPartType.this.item );
			final C wrapper = GunPartType.this.getContexted( stack );
			wrapper.setBase( this, 0 ); // See ModuleWrapper#setBase(...).
			return wrapper;
		}
		
		@SuppressWarnings( "unchecked" )
		protected final C self() { return ( C ) this; }
		
		protected abstract IEquippedItem< ? > newEquipped( EntityPlayer player, EnumHand hand );
		
		protected abstract IEquippedItem< ? > copyEquipped(
			IEquippedItem< ? > target,
			EntityPlayer player,
			EnumHand hand
		);
		
		protected class EquippedGunPart implements IEquippedItem< C >
		{
			@SideOnly( Side.CLIENT )
			protected ER renderer;
			
			@SideOnly( Side.CLIENT )
			protected Function< E, E > renderDelegate;
			
			protected EquippedGunPart( EntityPlayer player, EnumHand hand )
			{
				if ( player.world.isRemote )
				{
					this.renderer = GunPart.this.renderer.onTakeOut( hand );
					this.renderDelegate = original -> original;
				}
			}
			
			@SuppressWarnings( "unchecked" )
			protected EquippedGunPart(
				IEquippedItem< ? > prevEquipped,
				EntityPlayer player,
				EnumHand ignored
			) {
				if ( player.world.isRemote )
				{
					final EquippedGunPart prev = ( EquippedGunPart ) prevEquipped;
					this.renderer = prev.renderer;
					this.renderDelegate = prev.renderDelegate;
				}
			}
			
			@Override
			@SuppressWarnings( "unchecked" )
			public C item() { return ( C ) GunPart.this.base; }
			
			@Override
			public void tickInHand( EntityPlayer player, EnumHand hand )
			{
				if ( player.world.isRemote ) {
					this.renderer.tickInHand( this.renderDelegate(), hand );
				}
			}
			
			@Override
			@SideOnly( Side.CLIENT )
			public void updateAnimationForRender( EnumHand hand ) {
				this.renderer.updateAnimationForRender( this.renderDelegate(), hand );
			}
			
			@Override
			@SideOnly( Side.CLIENT )
			public void prepareRenderInHandSP( EnumHand hand ) {
				this.renderer.prepareRenderInHandSP( this.renderDelegate(), hand );
			}
			
			@Override
			@SideOnly( Side.CLIENT )
			public boolean renderInHandSP( EnumHand hand ) {
				return this.renderer.renderInHandSP( this.renderDelegate(), hand );
			}
			
			@Override
			@SideOnly( Side.CLIENT )
			public boolean onRenderSpecificHandSP( EnumHand hand ) {
				return this.renderer.onRenderSpecificHandSP( this.renderDelegate(), hand );
			}
			
			@Override
			@SideOnly( Side.CLIENT )
			public IAnimator animator() { return this.renderer.animator(); }
			
			@Override
			@SideOnly( Side.CLIENT )
			public void onKeyPress( IInput key )
			{
				final boolean isModifyInput = key.category().equals( Key.Category.MODIFY );
				if ( isModifyInput )
				{
					final IOperation executing = PlayerPatchClient.instance.executing();
					final boolean isModifying = executing instanceof OpModifyClient;
					if ( isModifying ) {
						( ( OpModifyClient ) executing ).handleInput( key );
					}
					return;
				}
				
				final boolean toggleModify = (
					key == Key.TOGGLE_MODIFY
					|| key == Key.CO_TOGGLE_MODIFY
				);
				if ( toggleModify )
				{
					final IOperation executing = PlayerPatchClient.instance.executing();
					if ( executing instanceof OpModifyClient )
					{
						PlayerPatchClient.instance.toggleExecuting();
						return;
					}
					
					// Launch modify operation.
					final OpModifyClient modifyOp = new OpModifyClient( this ) {
						@Override
						@SuppressWarnings( "unchecked" )
						protected IModule< ? > replicateDelegatePrimary()
						{
							final EquippedGunPart equipped = ( EquippedGunPart ) this.equipped;
							final E copied = ( E ) equipped.copy();
							equipped.renderDelegate = original -> copied;
							return copied.item();
						}
						
						@Override
						@SuppressWarnings( "unchecked" )
						protected void endCallback()
						{
							final EquippedGunPart equipped = ( EquippedGunPart ) this.equipped;
							equipped.renderDelegate = original -> original;
							equipped.renderer.useOperateAnimation( IAnimator.NONE );
						}
					};
					PlayerPatchClient.instance.launch( modifyOp );
					this.renderer.useModifyAnimation(
						modifyOp::smoothedProgress,
						() -> modifyOp.refPlayerRotYaw
					);
				}
			}
			
			@SideOnly( Side.CLIENT )
			@SuppressWarnings( "unchecked" )
			protected final E renderDelegate() { return this.renderDelegate.apply( ( E ) this ); }
			
			@SideOnly( Side.CLIENT )
			@SuppressWarnings( "unchecked" )
			protected IEquippedItem< ? > copy()
			{
				final ItemStack copiedStack = GunPart.this.base.toStack().copy();
				final IModule< ? > wrapper = ( IModule< ? > ) IItemTypeHost.getItem( copiedStack );
				final GunPart copied = ( GunPart ) wrapper.getInstalled( null, 0 );
				return copied.copyEquipped(
					EquippedGunPart.this,
					FMUMClient.MC.player,
					EnumHand.MAIN_HAND
				);
			}
			
			@Override
			public String toString() { return "Equipped<" + GunPartType.this + ">"; }
		}
	}
}
