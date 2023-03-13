package com.mcwb.common.gun;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;
import java.util.function.Function;

import com.google.gson.annotations.SerializedName;
import com.mcwb.client.MCWBClient;
import com.mcwb.client.gun.IGunPartRenderer;
import com.mcwb.client.item.IEquippedItemRenderer;
import com.mcwb.client.module.IDeferredPriorityRenderer;
import com.mcwb.client.module.IDeferredRenderer;
import com.mcwb.client.render.IAnimator;
import com.mcwb.common.item.IEquippedItem;
import com.mcwb.common.item.IItem;
import com.mcwb.common.item.IItemTypeHost;
import com.mcwb.common.item.ItemType;
import com.mcwb.common.load.IContentProvider;
import com.mcwb.common.meta.IMeta;
import com.mcwb.common.module.IModule;
import com.mcwb.common.module.IModuleSlot;
import com.mcwb.common.module.IModuleType;
import com.mcwb.common.module.Module;
import com.mcwb.common.module.ModuleSnapshot;
import com.mcwb.common.paintjob.IPaintableType;
import com.mcwb.common.paintjob.IPaintjob;
import com.mcwb.devtool.Dev;
import com.mcwb.util.ArmTracker;
import com.mcwb.util.Mat4f;

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

public abstract class GunPartType<
	C extends IGunPart< ? >,
	E extends IEquippedItem< ? extends C >,
	R extends IGunPartRenderer< ? super C, ? super E >
> extends ItemType< C, E, R > implements IModuleType, IPaintableType, IPaintjob
{
	protected static final float[] OFFSETS = { 0F };
	
	@SerializedName( value = "category", alternate = "group" )
	protected String category;
	
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
		
		// If not category set then set it is its name
		this.category = this.category != null ? this.category : this.name;
		provider.clientOnly( () ->
			this.modifyIndicator = this.modifyIndicator != null
				? this.modifyIndicator : MCWBClient.MODIFY_INDICATOR
		);
		
		// Add itself as the default paintjob
		if( this.paintjobs.size() == 0 )
			this.paintjobs = new ArrayList<>();
		this.paintjobs.add( 0, this );
		
		// Apply model scale
		this.slots.forEach( slot -> slot.scale( this.paramScale ) );
		// TODO: hitboxes
		return this;
	}
	
	@Override
	public void onPostLoad()
	{
		super.onPostLoad();
		
		this.provider.clientOnly( () -> {
			if( IModuleType.REGISTRY.get( this.modifyIndicator ) == null )
			{
				this.error( "mcwb.fail_to_find_indicator", this, this.modifyIndicator );
				this.modifyIndicator = MCWBClient.MODIFY_INDICATOR;
			}
		} );
		
		// TODO: call update state maybe?
		final Function< String, IModule< ? > > func = name -> this.newRawContexted();
		this.compiledSnapshotNBT = this.snapshot.setSnapshot( func ).serializeNBT();
	}
	
	@Override
	public void injectPaintjob( IPaintjob paintjob ) { this.paintjobs.add( paintjob ); }
	
	@Override
	@SideOnly( Side.CLIENT )
	public ResourceLocation texture() { return this.texture; }
	
	// TODO: handle paintjobs
	@Override
	protected Item createItem() { return this.new GunPartVanillaItem( 1, 0 ); }
	
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
			// 4 case to handle: \
			// has-stackTag | has-capTag}: {ItemStack#ItemStack(NBTTagCompound)} \
			// has-stackTag | no--capTag}: \
			// no--stackTag | has-capTag}: {ItemStack#copy()} \
			// no--stackTag | no--capTag}: {new ItemStack(...)}, {PacketBuffer#readItemStack()} \
			final NBTTagCompound stackTag = stack.getTagCompound();
			final GunPartType< C, E, R > $this = GunPartType.this;
			
			C primary;
			if( capTag != null )
			{
				// 2 cases possible:
				// no--stackTag | has-capTag: {ItemStack#copy()}
				// has-stackTag | has-capTag: {ItemStack#ItemStack(NBTTagCompound)}
				final NBTTagCompound nbt = capTag.getCompoundTag( "Parent" );
				
				// Remove "Parent" tag to prevent repeat deserialization
				// See CapabilityDispatcher#deserializeNBT(NBTTagCompound)
				capTag.removeTag( "Parent" );
				
				NBTTagCompound primaryTag;
				if( stackTag == null )
				{
					// Has to copy before use if is first case as the capability tag provided here \
					// could be the same as the bounden tag of copy target.
					primaryTag = nbt.copy();
					
					// To ensure #syncAndUpdate() call will not crash on null
					stack.setTagCompound( new NBTTagCompound() ); // TODO: static instance
				}
				else primaryTag = nbt;
				
				primary = ( C ) $this.fromTag( primaryTag );
			}
			else if( stackTag != null )
			{
				// has-stackTag | no--capTag: should never happen
				Dev.cur();
				throw new RuntimeException( "has-stackTag | no--capTag: should never happen" );
			}
			else
			{
				// no--stackTag | no--capTag: {new ItemStack(...)}, {PacketBuffer#readItemStack()}
				// We basically has no way to distinguish from these two cases. But it will work \
				// fine if we simply deserialize and setup it with the compiled snapshot NBT. That \
				// is because #readNBTShareTag(ItemStack, NBTTagCompound) will later be called for \
				// the network packet case. The down side is that it will actually deserialize \
				// twice for the network packet case.
				final NBTTagCompound newStackTag = new NBTTagCompound();
				newStackTag.setInteger( "i", new Random().nextInt() ); // TODO: better way to do this?
				// See GunPartWrapper#stackId()
				
				final NBTTagCompound primaryTag = $this.compiledSnapshotNBT.copy();
				primary = ( C ) $this.deserializeContexted( primaryTag );
			}
			
			final ICapabilityProvider wrapper = $this.newWrapper( primary, stack );
			primary.syncAndUpdate();
			return wrapper;
		}
		
		@Override
		public void readNBTShareTag( ItemStack stack, NBTTagCompound nbt )
		{
			stack.setTagCompound( nbt ); // Copied from super
			
			// See GunPartWrapper#syncNBTData()
			final NBTTagCompound primaryTag = nbt.getCompoundTag( "_" );
			final IModule< ? > primary = GunPartType.this.fromTag( primaryTag );
			
			final IModule< ? > wrapper = GunPartType.this.getContexted( stack );
			wrapper.setBase( primary, -1 ); // See GunPartWrapper#setBase(...)
			wrapper.syncAndUpdate();
		}
		
		/**
		 * <p> {@inheritDoc} </p>
		 * 
		 * <p> In default avoid to break the block when holding a this item in survive mode. </p>
		 */
		@Override
		public boolean onBlockStartBreak(
			ItemStack itemstack,
			BlockPos pos,
			EntityPlayer player
		) { return true; }
		
		/**
		 * <p> {@inheritDoc} </p>
		 * 
		 * <p> In default avoid to break the block when holding this item in creative mode. </p>
		 */
		@Override
		public boolean canDestroyBlockInCreative(
			World world,
			BlockPos pos,
			ItemStack stack,
			EntityPlayer player
		) { return false; }
	}
	
	protected class GunPart< T extends IGunPart< ? extends T > >
		extends Module< T > implements IGunPart< T >
	{
		protected short offset;
		protected short step;
		
		protected GunPart() { }
		
		protected GunPart( boolean unused ) { super( unused ); }
		
		@Override
		public int stackId() { throw new RuntimeException(); }
		
		@Override
		public IEquippedItem< ? > onTakeOut( EntityPlayer player, EnumHand hand )
		{
			return this.newEquipped(
				player.world.isRemote
				? setter -> setter.accept( GunPartType.this.renderer.onTakeOut( hand ) )
				: setter -> { }
			);
		}
		
//		@Override
//		@SuppressWarnings( "unchecked" )
//		public IEquippedItem< ? > onStackUpdate(
//			IEquippedItem< ? > prevEquipped,
//			EntityPlayer player,
//			EnumHand hand
//		) {
//			return this.newEquipped(
//				player.world.isRemote
//				? setter -> setter.accept( ( ( EquippedGunPart ) prevEquipped ).renderer )
//				: setter -> { }
//			);
//		}
		
		@Override
		public String name() { return GunPartType.this.name; }
		
		@Override
		public String category() { return GunPartType.this.category; }
		
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
		public void applyTransform( int slot, IModule< ? > module, Mat4f dst )
		{
			dst.mul( this.mat );
			GunPartType.this.slots.get( slot ).applyTransform( module, dst );
		}
		
		@Override
		@SideOnly( Side.CLIENT )
		public void prepareRender(
			Collection< IDeferredRenderer > renderQueue0,
			Collection< IDeferredPriorityRenderer > renderQueue1,
			IAnimator animator
		) {
			
		}
		
		@Override
		@SideOnly( Side.CLIENT )
		public void prepareInHandRenderSP(
			Collection< IDeferredRenderer > renderQueue0,
			Collection< IDeferredPriorityRenderer > renderQueue1,
			IAnimator animator
		) {
			
		}
		
		@Override
		@SideOnly( Side.CLIENT )
		public void setupLeftArmToRender( ArmTracker leftArm, IAnimator animator )
		{
			final IAnimator wrappedAnimator = this.wrapAnimator( animator );
			GunPartType.this.renderer.setupLeftArmToRender( leftArm, wrappedAnimator );
		}
		
		@Override
		@SideOnly( Side.CLIENT )
		public void setupRightArmToRender( ArmTracker rightArm, IAnimator animator )
		{
			final IAnimator wrappedAnimator = this.wrapAnimator( animator );
			GunPartType.this.renderer.setupRightArmToRender( rightArm, wrappedAnimator );
		}
		
		@Override
		@SideOnly( Side.CLIENT )
		public ResourceLocation texture() {
			return GunPartType.this.paintjobs.get( this.paintjob ).texture();
		}
		
		@Override
		@SideOnly( Side.CLIENT )
		public IModule< ? > newModifyIndicator() {
			return IModuleType.REGISTRY.get( GunPartType.this.modifyIndicator ).newRawContexted();
			// TODO: maybe a buffered instance
		}
		
		@Override
		public String toString() { return "Contexted<" + GunPartType.this + ">"; }
		
		// TODO: move to outer layer
		protected IEquippedItem< ? > newEquipped(
			Consumer< Consumer< IEquippedItemRenderer< ? super E > > > setter
		) { return this.new EquippedGunPart( setter ); }
		
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
			wrapper.setBase( this, 0 ); // See ModuleWrapper#setBase(...)
			return wrapper;
		}
		
		@SideOnly( Side.CLIENT )
		protected IAnimator wrapAnimator( IAnimator animtor )
		{
			// FIXME: 
			return IAnimator.INSTANCE;
		}
		
		@SuppressWarnings( "unchecked" )
		protected final C self() { return ( C ) this; }
		
		protected class EquippedGunPart implements IEquippedItem< C >
		{
			@SideOnly( Side.CLIENT )
			protected IEquippedItemRenderer< ? super E > renderer;
			
			// A bit of tricky to ensure that this will not break on server side
			protected EquippedGunPart(
				Consumer< Consumer< IEquippedItemRenderer< ? super E > > > setter
			) { setter.accept( renderer -> this.renderer = renderer ); }
			
			@Override
			public C item() { return GunPart.this.self(); }
			
			@Override
			@SuppressWarnings( "unchecked" )
			public IEquippedItem< ? > onStackUpdate(
				IItem newItem,
				EntityPlayer player,
				EnumHand hand
			) {
				return ( ( GunPart< T > ) newItem ).new EquippedGunPart(
					player.world.isRemote
					? setter -> setter.accept( this.renderer )
					: setter -> { }
				);
			}
			
			@Override
			@SideOnly( Side.CLIENT )
			public void prepareRenderInHandSP( EnumHand hand ) {
				this.renderer.prepareRenderInHandSP( this.self(), hand );
			}
			
			@Override
			@SideOnly( Side.CLIENT )
			public boolean renderInHandSP( EnumHand hand ) {
				return this.renderer.renderInHandSP( this.self(), hand );
			}
			
			@Override
			@SideOnly( Side.CLIENT )
			public boolean onRenderSpecificHandSP( EnumHand hand ) {
				return this.renderer.onRenderSpecificHandSP( this.self(), hand );
			}
			
			@Override
			@SideOnly( Side.CLIENT )
			public IAnimator animator() { return this.renderer.animator(); }
			
			@SideOnly( Side.CLIENT )
			@SuppressWarnings( "unchecked" )
			protected final E self() { return ( E ) this; }
		}
	}
}
