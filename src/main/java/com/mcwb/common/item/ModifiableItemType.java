package com.mcwb.common.item;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import javax.annotation.Nullable;

import com.google.gson.annotations.SerializedName;
import com.mcwb.client.MCWBClient;
import com.mcwb.client.input.IKeyBind;
import com.mcwb.client.input.Key;
import com.mcwb.client.input.Key.Category;
import com.mcwb.client.item.IItemRenderer;
import com.mcwb.client.module.IDeferredPriorityRenderer;
import com.mcwb.client.module.IDeferredRenderer;
import com.mcwb.client.module.IModuleRenderer;
import com.mcwb.client.player.OpModifyClient;
import com.mcwb.client.player.PlayerPatchClient;
import com.mcwb.client.render.IAnimator;
import com.mcwb.common.load.IContentProvider;
import com.mcwb.common.meta.IMeta;
import com.mcwb.common.module.IModular;
import com.mcwb.common.module.IModularType;
import com.mcwb.common.module.IModuleSlot;
import com.mcwb.common.module.Module;
import com.mcwb.common.module.ModuleSnapshot;
import com.mcwb.common.module.ModuleWrapper;
import com.mcwb.common.paintjob.IPaintable;
import com.mcwb.common.paintjob.IPaintableType;
import com.mcwb.common.paintjob.IPaintjob;
import com.mcwb.devtool.Dev;
import com.mcwb.util.Mat4f;

import net.minecraft.entity.Entity;
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

public abstract class ModifiableItemType<
	C extends IItem & IModular< ? > & IPaintable,
	R extends IItemRenderer< ? super C > & IModuleRenderer< ? super C >
> extends ItemType< C, R > implements IModularType, IPaintableType, IPaintjob
{
	// TODO: side only
	public static final OpModifyClient OP_MODIFY = new OpModifyClient();
	
	@SerializedName( value = "category", alternate = "group" )
	protected String category;
	
	@SerializedName( value = "paramScale", alternate = "scale" )
	protected float paramScale = 1F;
	
	@SerializedName( value = "slots", alternate = "moduleSlots" )
	protected List< IModuleSlot > slots = Collections.emptyList();
	
	@SerializedName( value = "snapshot", alternate = "preInstalls" )
	protected ModuleSnapshot snapshot = ModuleSnapshot.DEFAULT;
	protected transient NBTTagCompound compiledSnapshotNBT;
	
	@SerializedName( value = "paintjobs", alternate = "skins" )
	protected List< IPaintjob > paintjobs = Collections.emptyList();
	
	@SideOnly( Side.CLIENT )
	protected String modifyIndicator;
	
	@Override
	public IMeta build( String name, IContentProvider provider )
	{
		super.build( name, provider );
		
		IModularType.REGISTRY.regis( this );
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
			if( IModularType.REGISTRY.get( this.modifyIndicator ) == null )
			{
				this.error( "mcwb.fail_to_find_indicator", this, this.modifyIndicator );
				this.modifyIndicator = MCWBClient.MODIFY_INDICATOR;
			}
		} );
		
		// TODO: call update state maybe?
		final Function< String, IModular< ? > > func = name -> this.newRawContexted();
		this.compiledSnapshotNBT = this.snapshot.setSnapshot( func ).serializeNBT();
	}
	
	@Override
	public void injectPaintjob( IPaintjob paintjob ) { this.paintjobs.add( paintjob ); }
	
	@Override
	@SideOnly( Side.CLIENT )
	public ResourceLocation texture() { return this.texture; }
	
	// TODO: handle paintjobs
	@Override
	protected Item createItem() { return this.new ModifiableVanillaItem( 1, 0 ); }
	
	protected final IModular< ? > fromTag( NBTTagCompound tag )
	{
		final Item item = Item.getItemById( IModular.getId( tag ) );
		final IModularType type = ( IModularType ) IItemTypeHost.getType( item );
		return type.deserializeContexted( tag );
	}
	
	protected abstract ICapabilityProvider newWrapper( C primary, ItemStack stack );
	
	protected class ModifiableVanillaItem extends VanillaItem
	{
		protected ModifiableVanillaItem( int maxStackSize, int maxDamage ) {
			super( maxStackSize, maxDamage );
		}
		
		@Override
		@SuppressWarnings( "unchecked" )
		public ICapabilityProvider initCapabilities(
			ItemStack stack,
			@Nullable NBTTagCompound capTag
		) {
//			
//			// 4 case to handle: \
//			// has-stackTag | has-capTag}: {deserialized from local storage} \
//			// has-stackTag | no--capTag}: \
//			// no--stackTag | has-capTag}: {copy stack} \
//			// no--stackTag | no--capTag}: {create stack}, {deserialize from network packet} \
			final NBTTagCompound stackTag = stack.getTagCompound();
			final ModifiableItemType< ?, ? > $this = ModifiableItemType.this;
			
			if( capTag != null )
			{
//				// 2 cases possible:
//				// no--stackTag | has-capTag: {copy stack}
//				// has-stackTag | has-capTag: {deserialize from local storage}
				final NBTTagCompound nbt = capTag.getCompoundTag( "Parent" );
//				
//				// Remove "Parent" tag to prevent repeat deserialization
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
				
				final C primary = ( C ) $this.fromTag( primaryTag );
				final ICapabilityProvider wrapper =
					ModifiableItemType.this.newWrapper( primary, stack );
				primary.syncAndUpdate();
				return wrapper;
			}
			
			// has-stackTag | no--capTag: should never happen
			if( stackTag != null )
			{
				Dev.cur();
				throw new RuntimeException( "has-stackTag | no--capTag: should never happen" );
			}
			
			// no--stackTag | no--capTag: {create stack}, {deserialize from network packet}
			// We basically has no way to distinguish from these two cases. But it will work fine \
			// if we simply deserialize and setup it with the compiled snapshot NBT. The down side \
			// is that it will actually deserialize twice for the network packet case.
			stack.setTagCompound( new NBTTagCompound() );
			
			final NBTTagCompound primaryTag = $this.compiledSnapshotNBT.copy();
			final C primary = ( C ) $this.deserializeContexted( primaryTag );
			final ICapabilityProvider provider =
				ModifiableItemType.this.newWrapper( primary, stack );
			primary.syncAndUpdate();
			return provider;
		}
		
		@Override
		public void readNBTShareTag( ItemStack stack, NBTTagCompound nbt )
		{
			stack.setTagCompound( nbt ); // Copied from super
			
			final NBTTagCompound primaryTag = nbt.getCompoundTag( "_" );
			final IModular< ? > primary = ModifiableItemType.this.fromTag( primaryTag );
			
			// Set ModuleWrapper#setBase(...)
			final IModular< ? > wrapper = ModifiableItemType.this.getContexted( stack );
			wrapper.setBase( primary, 0 );
			wrapper.syncAndUpdate();
		}
		
		// Cause we now have the wrapper so avoid to tick render from item
		// See ModifiableItem#tickInHand(...)
		@Override
		public void onUpdate(
			ItemStack stack,
			World worldIn,
			Entity entityIn,
			int itemSlot,
			boolean isSelected
		) { }
		
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
	
	protected abstract class ModifiableItem< T extends IModular< ? extends T > >
		extends Module< T > implements IItem
	{
		protected ModifiableItem() { }
		
		protected ModifiableItem( boolean unused ) { super( unused ); }
		
		@Override
		public String name() { return ModifiableItemType.this.name; }
		
		@Override
		public String category() { return ModifiableItemType.this.category; }
		
		@Override
		public int paintjobCount() { return ModifiableItemType.this.paintjobs.size(); }
		
		@Override
		public int slotCount() { return ModifiableItemType.this.slots.size(); }
		
		@Override
		public IModuleSlot getSlot( int idx ) { return ModifiableItemType.this.slots.get( idx ); }
		
		@Override
		public void tickInHand( EntityPlayer player, EnumHand hand )
		{
			if( player.world.isRemote )
				ModifiableItemType.this.renderer.tickInHand( this.self(), hand );
		}
		
		@Override
		@SideOnly( Side.CLIENT )
		public void prepareRenderInHand( EnumHand hand ) {
			ModifiableItemType.this.renderer.prepareRenderInHand( this.self(), hand );
		}
		
		@Override
		@SideOnly( Side.CLIENT )
		public boolean renderInHand( EnumHand hand ) {
			return ModifiableItemType.this.renderer.renderInHand( this.self(), hand );
		}
		
		@Override
		@SideOnly( Side.CLIENT )
		public boolean onRenderSpecificHand( EnumHand hand ) {
			return ModifiableItemType.this.renderer.onRenderSpecificHand( this.self(), hand );
		}
		
		@Override
		@SideOnly( Side.CLIENT )
		public void onKeyPress( IKeyBind key )
		{
			switch( key.name() )
			{
			case Key.TOGGLE_MODIFY:
			case Key.CO_TOGGLE_MODIFY:
				final PlayerPatchClient patch = PlayerPatchClient.instance;
				final OpModifyClient opModify = this.opModify();
				if( patch.executing() == opModify )
					patch.toggleExecuting();
				else patch.tryLaunch( opModify.reset() );
				break;
			}
			
			// For keys of category modify, just send to operation to handle them
			if( key.category().equals( Category.MODIFY ) )
				this.opModify().handleInput( key );
		}
		
		@Override
		public void applyTransform( int slot, IModular< ? > module, Mat4f dst )
		{
			dst.mul( this.mat );
			ModifiableItemType.this.slots.get( slot ).applyTransform( module, dst );
		}
		
		@Override
		@SideOnly( Side.CLIENT )
		public void prepareInHandRender(
			Collection< IDeferredRenderer > renderQueue0,
			Collection< IDeferredPriorityRenderer > renderQueue1,
			IAnimator animator
		) {
			ModifiableItemType.this.renderer.prepareInHandRender(
				this.self(),
				this.wrapAnimator( animator ),
				renderQueue0,
				renderQueue1
			);
			
			this.installed.forEach(
				mod -> mod.prepareInHandRender( renderQueue0, renderQueue1, animator )
			);
		}
		
		@Override
		@SideOnly( Side.CLIENT )
		public void prepareRender(
			Collection< IDeferredRenderer > renderQueue0,
			Collection< IDeferredPriorityRenderer > renderQueue1,
			IAnimator animator
		) {
			ModifiableItemType.this.renderer.prepareRender(
				this.self(),
				this.wrapAnimator( animator ),
				renderQueue0,
				renderQueue1
			);
			
			this.installed.forEach(
				mod -> mod.prepareRender( renderQueue0, renderQueue1, animator )
			);
		}
		
		@Override
		@SideOnly( Side.CLIENT )
		public ResourceLocation texture() {
			return ModifiableItemType.this.paintjobs.get( this.paintjob ).texture();
		}
		
		@Override
		@SideOnly( Side.CLIENT )
		public OpModifyClient opModify() { return OP_MODIFY; }
		
		@Override
		@SideOnly( Side.CLIENT )
		public IModular< ? > newModifyIndicator()
		{
			final String indicator = ModifiableItemType.this.modifyIndicator;
			return IModularType.REGISTRY.get( indicator ).newRawContexted();
			// TODO: maybe a buffered instance
		}
		
		@Override
		public String toString() { return "Contexted<" + ModifiableItemType.this + ">"; }
		
		@Override
		protected int id() { return Item.getIdFromItem( ModifiableItemType.this.item ); }
		
		@Override
		protected IModular< ? > fromTag( NBTTagCompound tag ) {
			return ModifiableItemType.this.fromTag( tag );
		}
		
		@Override
		protected IModular< ? > wrapOnBeingRemoved()
		{
			// Maybe a bit of too hacky...
			final ItemStack stack = new ItemStack( ModifiableItemType.this.item );
			final IModular< ? > wrapper = ModifiableItemType.this.getContexted( stack );
			wrapper.setBase( this, 0 ); // See ModuleWrapper#setBase(...)
			return wrapper;
		}
		
		@SideOnly( Side.CLIENT )
		protected IAnimator wrapAnimator( IAnimator animator )
		{
			return ( channel, smoother, dst ) -> {
				switch( channel )
				{
				case IModuleRenderer.CHANNEL_INSTALL:
					dst.mul( this.mat );
					break;
					
				default: animator.applyChannel( channel, smoother, dst );
				}
			};
		}
		
		@SideOnly( Side.CLIENT )
		@SuppressWarnings( "unchecked" )
		protected final C self() { return ( C ) this; }
	}
	
	protected static class ModifiableItemWrapper<
		M extends IModular< ? extends M >,
		T extends IItem & IModular< ? extends M > & IPaintable
	> extends ModuleWrapper< M, T > implements IItem
	{
		protected final ItemStack stack;
		
		protected ModifiableItemWrapper( T primary, ItemStack stack )
		{
			super( primary );
			
			this.stack = stack;
		}
		
		@Override
		public IUseContext onTakeOut( IItem oldItem, EntityPlayer player, EnumHand hand ) {
			return this.primary.onTakeOut( oldItem, player, hand );
		}
		
		@Override
		public ItemStack toStack() { return this.stack; }
		
		@Override
		public void tickInHand( EntityPlayer player, EnumHand hand ) {
			this.primary.tickInHand( player, hand );
		}
		
		@Override
		public IUseContext onInHandStackChanged(
			IItem oldItem,
			EntityPlayer player,
			EnumHand hand
		) { return this.primary.onInHandStackChanged( oldItem, player, hand ); }
		
		@Override
		public boolean onSwapHand( EntityPlayer player ) {
			return this.primary.onSwapHand( player );
		}
		
		@Override
		@SideOnly( Side.CLIENT )
		public void prepareRenderInHand( EnumHand hand ) {
			this.primary.opModify().delegate( this.primary ).prepareRenderInHand( hand );
		}
		
		@Override
		@SideOnly( Side.CLIENT )
		public boolean renderInHand( EnumHand hand ) {
			return this.primary.opModify().delegate( this.primary ).renderInHand( hand );
		}
		
		@Override
		@SideOnly( Side.CLIENT )
		public boolean onRenderSpecificHand( EnumHand hand ) {
			return this.primary.opModify().delegate( this.primary ).onRenderSpecificHand( hand );
		}
		
		@Override
		@SideOnly( Side.CLIENT )
		public void onKeyPress( IKeyBind key ) { this.primary.onKeyPress( key ); }
		
		@Override
		@SideOnly( Side.CLIENT )
		public void onKeyRelease( IKeyBind key ) { this.primary.onKeyRelease( key ); }
		
		@Override
		@SideOnly( Side.CLIENT )
		public boolean onMouseWheelInput( int dWheel ) {
			return this.primary.onMouseWheelInput( dWheel );
		}
		
		@Override
		@SideOnly( Side.CLIENT )
		public boolean updateViewBobbing( boolean original ) {
			return this.primary.updateViewBobbing( original );
		}
		
		@Override
		@SideOnly( Side.CLIENT )
		public boolean hideCrosshair() { return this.primary.hideCrosshair(); }
		
		@Override
		@SideOnly( Side.CLIENT )
		public ResourceLocation texture() { throw new RuntimeException(); }
		
		@Override
		protected void syncNBTData() {
			this.stack.getTagCompound().setTag( "_", this.primary.serializeNBT() );
		}
	}
}
