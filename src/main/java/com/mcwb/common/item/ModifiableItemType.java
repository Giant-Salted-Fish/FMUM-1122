package com.mcwb.common.item;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import javax.annotation.Nullable;

import com.google.gson.annotations.SerializedName;
import com.mcwb.client.input.IKeyBind;
import com.mcwb.client.item.IItemRenderer;
import com.mcwb.client.module.IDeferredPriorityRenderer;
import com.mcwb.client.module.IDeferredRenderer;
import com.mcwb.client.module.IModuleRenderer;
import com.mcwb.client.render.IAnimator;
import com.mcwb.common.load.IContentProvider;
import com.mcwb.common.meta.IMeta;
import com.mcwb.common.module.IModular;
import com.mcwb.common.module.IModularType;
import com.mcwb.common.module.IModuleSlot;
import com.mcwb.common.module.IModuleSnapshot;
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
	C extends IItem & IModular< ? > & IPaintable,
	R extends IItemRenderer< ? super C > & IModuleRenderer< ? super C >
> extends ItemType< C, R > implements IModularType, IPaintableType, IPaintjob
{
	/**
	 * TODO: explain what is this for
	 */
	protected static final NBTTagCompound NBT = new NBTTagCompound();
	
	@SerializedName( value = "category", alternate = "group" )
	protected String category;
	
	@SerializedName( value = "paramScale", alternate = "scale" )
	protected float paramScale = 1F;
	
	@SerializedName( value = "slots", alternate = "moduleSlots" )
	protected List< IModuleSlot > slots = Collections.emptyList();
	
	@SerializedName( value = "snapshot", alternate = "preInstalls" )
	protected IModuleSnapshot snapshot = ModuleSnapshot.DEFAULT;
	protected transient NBTTagCompound compiledSnapshotNBT;
	
	@SerializedName( value = "paintjobs", alternate = "skins" )
	protected List< IPaintjob > paintjobs = Collections.emptyList();
	
	@Override
	public IMeta build( String name, IContentProvider provider )
	{
		super.build( name, provider );
		
		IModularType.REGISTRY.regis( this );
		IPaintableType.REGISTRY.regis( this );
		
		// If not category set then set it is its name
		this.category = this.category != null ? this.category : this.name;
		
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
		
		// TODO: set a default indicator
		
		// TODO: call update state maybe?
		final Function< String, IModular< ? > > func = name -> this.newPreparedContexted();
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
	
	// TODO: seal this
	protected IModular< ? > fromTag( NBTTagCompound tag )
	{
		final Item item = Item.getItemById( IModular.getId( tag ) );
		final IModularType type = ( IModularType ) IItemTypeHost.getType( item );
		return type.deserializeContexted( tag );
	}
	
	protected abstract ModifiableItemWrapper< ?, ? > newWrapper(
		IModular< ? > primary,
		ItemStack stack
	);
	
	protected class ModifiableVanillaItem extends VanillaItem
	{
		protected ModifiableVanillaItem( int maxStackSize, int maxDamage ) {
			super( maxStackSize, maxDamage );
		}
		
		@Override
		public ICapabilityProvider initCapabilities(
			ItemStack stack,
			@Nullable NBTTagCompound capTag
		) {
			final NBTTagCompound stackTag = stack.getTagCompound();
			final ModifiableItemType< ?, ? > $this = ModifiableItemType.this;
			
			// 4 case to handle: \
			// has-stackTag | has-capTag}: {deserialized from local storage} \
			// has-stackTag | no--capTag}: \
			// no--stackTag | has-capTag}: {copy stack} \
			// no--stackTag | no--capTag}: {create stack}, {deserialize from network packet} \
			if( capTag != null )
			{
				// 2 cases possible:
				// no--stackTag | has-capTag: {copy stack}
				// has-stackTag | has-capTag: {deserialize from local storage}
				final NBTTagCompound nbt = capTag.getCompoundTag( "Parent" );
				
				// Remove "Parent" tag to prevent repeat deserialization
				capTag.removeTag( "Parent" );
				
				// Has to copy before use if is first case as the capability tag provided here \
				// could be the same as the bounden tag of copy target.
				final IModular< ? > primary = $this.fromTag( stack == null ? nbt.copy() : nbt );
				primary.updateState();
				
				// #syncNBTData() not called as it is possible that the stack tag has not been set \
				// yet. This would not cause problem because the stack tag also has the same data.
				return $this.newWrapper( primary, stack );
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
			final IModular< ? > primary = $this.deserializeContexted( primaryTag );
			primary.updateState();
			
			final ModifiableItemWrapper< ?, ? > wrapper = $this.newWrapper( primary, stack );
			wrapper.syncNBTData();
			return wrapper;
		}
		
		@Override
		public void readNBTShareTag( ItemStack stack, @Nullable NBTTagCompound nbt )
		{
			stack.setTagCompound( nbt ); // Copied from super
			
			final NBTTagCompound primaryTag = nbt.getCompoundTag( "_" );
			final IModular< ? > primary = ModifiableItemType.this.fromTag( primaryTag );
			ModifiableItemType.this.getContexted( stack ).setBase( primary, 0 );
			// See ModuleWrapper#setBase(...)
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
	
	protected abstract class ModifiableItem< T extends IModular< ? extends T > >
		extends Module< T > implements IItem
	{
		protected ModifiableItem() { }
		
		protected ModifiableItem( NBTTagCompound nbt ) { super( nbt ); }
		
		@Override
		public String name() { return ModifiableItemType.this.name; }
		
		@Override
		public String category() { return ModifiableItemType.this.category; }
		
		/**
		 * Maybe a kind of too hacky...
		 */
//		@Override
//		public IModular< ? > removeFromBase( int slot, int idx )
//		{
//			this.base.remove( slot, idx );
//			final ItemStack stack = new ItemStack( ModifiableItemType.this.item );
//			final IModular< ? > wrapper = ModifiableItemType.this.getContexted( stack );
//			wrapper.deserializeNBT( this.nbt );
//			
//			// TODO: notify the modules on wrapper that they are just removed from our base and can do something to update
//			
//			return wrapper;
//		}
		
		@Override
		public int paintjobCount() { return ModifiableItemType.this.paintjobs.size(); }
		
		@Override
		public int slotCount() { return ModifiableItemType.this.slots.size(); }
		
		@Override
		public IModuleSlot getSlot( int idx ) { return ModifiableItemType.this.slots.get( idx ); }
		
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
			// TODO
			switch( key.name() )
			{
			
			}
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
			// TODO: May not be necessary to call this every time if no item transform applied
//			this.mat.setIdentity();
//			this.base.applyTransform( this.baseSlot, this, this.mat );
			
			// TODO: maybe avoid instantiation to improve performance?
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
			// TODO: May not be necessary to call this every time if no item transform applied
//			this.mat.setIdentity();
//			this.base.applyTransform( this.baseSlot, this, this.mat );
			
			// TODO: maybe avoid instantiation to improve performance?
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
		protected int id() { return Item.getIdFromItem( ModifiableItemType.this.item ); }
		
		@Override
		protected IModular< ? > fromTag( NBTTagCompound tag ) {
			return ModifiableItemType.this.fromTag( tag );
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
		
		@SuppressWarnings( "unchecked" )
		protected C self() { return ( C ) this; }
	}
	
	protected static class ModifiableItemWrapper<
		M extends IModular< ? extends M >,
		T extends IItem & IModular< ? extends M > & IPaintable
	> extends ModuleWrapper< M, T > implements IItem
	{
		protected final ItemStack stack;
		
		protected ModifiableItemWrapper( IModular< ? > primary, ItemStack stack )
		{
			super( primary );
			
			this.stack = stack;
		}
		
		@Override
		@SideOnly( Side.CLIENT )
		public boolean renderInHand( EnumHand hand ) { return this.primary.renderInHand( hand ); }
		
		@Override
		@SideOnly( Side.CLIENT )
		public boolean onRenderSpecificHand( EnumHand hand ) {
			return this.primary.onRenderSpecificHand( hand );
		}
		
		@Override
		@SideOnly( Side.CLIENT )
		public ResourceLocation texture() { return this.primary.texture(); }
		
		@Override
		protected void syncNBTData() {
			this.stack.getTagCompound().setTag( "_", this.primary.serializeNBT() );
		}
	}
}
