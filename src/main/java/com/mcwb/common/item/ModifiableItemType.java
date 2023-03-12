package com.mcwb.common.item;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import javax.annotation.Nullable;

import com.google.gson.annotations.SerializedName;
import com.mcwb.client.MCWBClient;
import com.mcwb.client.item.IItemRenderer;
import com.mcwb.client.module.IModuleRenderer;
import com.mcwb.client.render.IAnimator;
import com.mcwb.common.load.IContentProvider;
import com.mcwb.common.meta.IMeta;
import com.mcwb.common.module.IModule;
import com.mcwb.common.module.IModuleSlot;
import com.mcwb.common.module.IModuleType;
import com.mcwb.common.module.Module;
import com.mcwb.common.module.ModuleSnapshot;
import com.mcwb.common.module.ModuleWrapper;
import com.mcwb.common.paintjob.IPaintable;
import com.mcwb.common.paintjob.IPaintableType;
import com.mcwb.common.paintjob.IPaintjob;
import com.mcwb.devtool.Dev;
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

public abstract class ModifiableItemType<
	C extends IItem & IModule< ? > & IPaintable,
	E extends IEquippedItem< C >,
	R extends IItemRenderer< ? super E > & IModuleRenderer< ? super E >
> extends ItemType< C, E, R > implements IModuleType, IPaintableType, IPaintjob
{
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
	protected Item createItem() { return this.new ModifiableVanillaItem( 1, 0 ); }
	
	protected final IModule< ? > fromTag( NBTTagCompound tag )
	{
		final Item item = Item.getItemById( IModule.getId( tag ) );
		final IModuleType type = ( IModuleType ) IItemTypeHost.getType( item );
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
			// 4 case to handle: \
			// has-stackTag | has-capTag}: {ItemStack#ItemStack(NBTTagCompound)} \
			// has-stackTag | no--capTag}: \
			// no--stackTag | has-capTag}: {ItemStack#copy()} \
			// no--stackTag | no--capTag}: {new ItemStack(...)}, {PacketBuffer#readItemStack()} \
			final NBTTagCompound stackTag = stack.getTagCompound();
			final ModifiableItemType< C, E, R > $this = ModifiableItemType.this;
			
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
				
				final C primary = ( C ) $this.fromTag( primaryTag );
				final ICapabilityProvider wrapper = $this.newWrapper( primary, stack );
				primary.syncAndUpdate();
				return wrapper;
			}
			
			// has-stackTag | no--capTag: should never happen
			if( stackTag != null )
			{
				Dev.cur();
				throw new RuntimeException( "has-stackTag | no--capTag: should never happen" );
			}
			
			// no--stackTag | no--capTag: {new ItemStack(...)}, {PacketBuffer#readItemStack()}
			// We basically has no way to distinguish from these two cases. But it will work fine \
			// if we simply deserialize and setup it with the compiled snapshot NBT. That is \
			// because Item#readNBTShareTag(ItemStack, NBTTagCompound) will later be called in the \
			// second case. Down side is that we do deserialization twice in the second case.
			stack.setTagCompound( new NBTTagCompound() );
			
			final NBTTagCompound primaryTag = $this.compiledSnapshotNBT.copy();
			final C primary = ( C ) $this.deserializeContexted( primaryTag );
			final ICapabilityProvider wrapper = $this.newWrapper( primary, stack );
			primary.syncAndUpdate();
			return wrapper;
		}
		
		@Override
		public void readNBTShareTag( ItemStack stack, NBTTagCompound nbt )
		{
			stack.setTagCompound( nbt ); // Copied from super
			
			final NBTTagCompound primaryTag = nbt.getCompoundTag( "_" );
			final IModule< ? > primary = ModifiableItemType.this.fromTag( primaryTag );
			
			final IModule< ? > wrapper = ModifiableItemType.this.getContexted( stack );
			wrapper.setBase( primary, 0 ); // Set ModuleWrapper#setBase(...)
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
	
	protected abstract class ModifiableItem< T extends IModule< ? extends T > >
		extends Module< T > implements IItem
	{
		protected ModifiableItem() { }
		
		protected ModifiableItem( boolean unused ) { super( unused ); }
		
		@Override
		public IEquippedItem< ? > onTakeOut(
			IEquippedItem< ? > prevEquipped,
			EntityPlayer player,
			EnumHand hand
		) {
			final R renderer = ModifiableItemType.this.renderer;
			return this.newEquipped( renderer.onTakeOut( prevEquipped, hand ) );
		}
		
		@Override
		public IEquippedItem< ? > onStackUpdate(
			IEquippedItem< ? > prevEquipped,
			EntityPlayer player,
			EnumHand hand
		) { return this.newEquipped( prevEquipped.animator() ); }
		
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
		public void applyTransform( int slot, IModule< ? > module, Mat4f dst )
		{
			dst.mul( this.mat );
			ModifiableItemType.this.slots.get( slot ).applyTransform( module, dst );
		}
		
		@Override
		@SideOnly( Side.CLIENT )
		public ResourceLocation texture() {
			return ModifiableItemType.this.paintjobs.get( this.paintjob ).texture();
		}
		
		@Override
		@SideOnly( Side.CLIENT )
		public IModule< ? > newModifyIndicator()
		{
			final String indicator = ModifiableItemType.this.modifyIndicator;
			return IModuleType.REGISTRY.get( indicator ).newRawContexted();
			// TODO: maybe a buffered instance
		}
		
		@Override
		public String toString() { return "Contexted<" + ModifiableItemType.this + ">"; }
		
		protected abstract IEquippedItem< ? > newEquipped( IAnimator animator );
		
		@Override
		protected int id() { return Item.getIdFromItem( ModifiableItemType.this.item ); }
		
		@Override
		protected final IModule< ? > fromTag( NBTTagCompound tag ) {
			return ModifiableItemType.this.fromTag( tag );
		}
		
		// Maybe a bit of too hacky...
		@Override
		protected IModule< ? > wrapOnBeingRemoved()
		{
			final ItemStack stack = new ItemStack( ModifiableItemType.this.item );
			final IModule< ? > wrapper = ModifiableItemType.this.getContexted( stack );
			wrapper.setBase( this, 0 ); // See ModuleWrapper#setBase(...)
			return wrapper;
		}
		
		@SideOnly( Side.CLIENT )
		@SuppressWarnings( "unchecked" )
		protected final C self() { return ( C ) this; }
	}
	
	protected static class ModifiableItemWrapper<
		M extends IModule< ? extends M >,
		T extends IItem & IModule< ? extends M > & IPaintable
	> extends ModuleWrapper< M, T > implements IItem
	{
		protected final ItemStack stack;
		
		protected ModifiableItemWrapper( T primary, ItemStack stack )
		{
			super( primary );
			
			this.stack = stack;
		}
		
		@Override
		public final ItemStack toStack() { return this.stack; }
		
		@Override
		public final int stackId() { return this.primary.stackId(); }
		
		@Override
		public final IEquippedItem< ? > onTakeOut(
			IEquippedItem< ? > prevEquipped,
			EntityPlayer player,
			EnumHand hand
		) { return this.primary.onTakeOut( prevEquipped, player, hand ); }
		
		@Override
		public final IEquippedItem< ? > onStackUpdate(
			IEquippedItem< ? > prevEquipped,
			EntityPlayer player,
			EnumHand hand
		) { return this.primary.onStackUpdate( prevEquipped, player, hand ); }
		
		@Override
		@SideOnly( Side.CLIENT )
		public final ResourceLocation texture() { throw new RuntimeException(); }
		
		@Override
		protected final void syncNBTData() {
			this.stack.getTagCompound().setTag( "_", this.primary.serializeNBT() );
		}
	}
}
