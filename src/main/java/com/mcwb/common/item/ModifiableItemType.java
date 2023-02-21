package com.mcwb.common.item;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;

import com.google.gson.annotations.SerializedName;
import com.mcwb.client.MCWBClient;
import com.mcwb.client.input.IKeyBind;
import com.mcwb.client.input.Key;
import com.mcwb.client.input.Key.KeyCategory;
import com.mcwb.client.item.IItemRenderer;
import com.mcwb.client.modify.IDeferredPriorityRenderer;
import com.mcwb.client.modify.IDeferredRenderer;
import com.mcwb.client.modify.IModifiableRenderer;
import com.mcwb.client.player.OpModifyClient;
import com.mcwb.client.player.PlayerPatchClient;
import com.mcwb.client.render.IAnimator;
import com.mcwb.common.MCWB;
import com.mcwb.common.meta.IMeta;
import com.mcwb.common.modify.IModifiable;
import com.mcwb.common.modify.IModifiableType;
import com.mcwb.common.modify.IModuleSlot;
import com.mcwb.common.modify.IModuleSnapshot;
import com.mcwb.common.modify.Modifiable;
import com.mcwb.common.modify.ModuleSnapshot;
import com.mcwb.common.modify.ModuleWrapper;
import com.mcwb.common.pack.IContentProvider;
import com.mcwb.common.paintjob.IPaintjob;
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
	C extends IItem & IModifiable,
	M extends IItemRenderer< ? super C > & IModifiableRenderer< ? super C >
> extends ItemType< C, M > implements IModifiableType, IPaintjob
{
	@SideOnly( Side.CLIENT )
	public static final OpModifyClient OP_MODIFY; static {
		OP_MODIFY = MCWB.MOD.isClient() ? new OpModifyClient() : null;
	}
	
	@SerializedName( value = "category", alternate = "group" )
	protected String category;
	
	@SerializedName( value = "paramScale", alternate = "scale" )
	protected float paramScale = 1F;
	
	@SerializedName( value = "slots", alternate = "moduleSlots" )
	protected List< IModuleSlot > slots = Collections.emptyList();
	
	@SerializedName( value = "snapshot", alternate = "preInstalls" )
	protected IModuleSnapshot snapshot = ModuleSnapshot.DEFAULT;
	protected transient NBTTagCompound snapshotNBT;
	
	@SerializedName( value = "paintjobs", alternate = "skins" )
	protected List< IPaintjob > paintjobs = Collections.emptyList();
	
	@SideOnly( Side.CLIENT )
	protected String modifyIndicator = MCWBClient.MODIFY_INDICATOR;
	
	@Override
	public IMeta build( String name, IContentProvider provider )
	{
		super.build( name, provider );
		
		IModifiableType.REGISTRY.regis( this );
		
		// If category is not set then set it to its name
		if( this.category == null )
			this.category = this.name;
		
		// Add itself as the default paintjob
		if( this.paintjobs.size() == 0 )
			this.paintjobs = new ArrayList<>();
		this.paintjobs.add( 0, this );
		
		// Apply model scale
		this.slots.forEach( slot -> slot.scale( this.paramScale ) );
		// TODO: hit boxes
		
		return this;
	}
	
	@Override
	public void onPostLoad()
	{
		super.onPostLoad();
		
		// Set a default indicator if it is not valid
		this.clientOnly( () -> {
			if( IModifiableType.REGISTRY.get( this.modifyIndicator ) == null )
			{
				this.error( "mcwb.fail_to_find_indicator", this, this.modifyIndicator );
				this.modifyIndicator = MCWBClient.MODIFY_INDICATOR;
			}
		} );
	}
	
	@Override
	public void prepareSnapshot()
	{
		this.snapshotNBT = this.snapshot.initContexted(
			name -> this.newContexted( new NBTTagCompound() )
		).serializeNBT();
	}
	
	@Override
	public void injectPaintjob( IPaintjob paintjob ) { this.paintjobs.add( paintjob ); }
	
	@Override
	@SideOnly( Side.CLIENT )
	public ResourceLocation texture() { return this.texture; }
	
	// TODO: handle paintjobs
	@Override
	protected Item createItem() { return this.new ModifiableVanillaItem( 1, 0 ); }
	
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
			
			// 4 case to handle: \
			// has-stackTag | has-capTag}: {deserialized from local storage} \
			// has-stackTag | no--capTag}: \
			// no--stackTag | has-capTag}: {copy stack} \
			// no--stackTag | no--capTag}: {create stack}, {deserialize from network packet} \
			final ModifiableItemType< ?, ? > type = ModifiableItemType.this;
			switch( ( stackTag != null ? 2 : 0 ) + ( capTag != null ? 1 : 0 ) )
			{
			case 0: // no--stackTag | no--capTag: {create stack}, {deserialize from network packet}
				final IModifiable primary0 = type.deserializeContexted( type.snapshotNBT.copy() );
				primary0.updatePrimaryState();
				
				stack.setTagCompound( new NBTTagCompound() );
				final ModuleWrapper wrapper = new ModuleWrapper( stack::getTagCompound, primary0 );
				wrapper.syncNBTData();
				return wrapper;
				
			case 1: // no--stackTag | has-capTag: {copy stack}
				// A little bit more work to do to handle copy case as the #serializeNBT() of the \
				// module could be its bounden tag, hence copy it before deserialize
				final NBTTagCompound copyiedTag = capTag.getCompoundTag( "Parent" ).copy();
				capTag.removeTag( "Parent" );
				
				final IModifiable primary1 = type.deserializeContexted( copyiedTag );
				primary1.updatePrimaryState();
				return new ModuleWrapper( stack::getTagCompound, primary1 );
				// #syncNBTData() not called as it is possible that the stack tag has not been set \
				// yet. This will not cause problem because the stack tag also has full context.
				
			case 2: // has-stackTag | no--capTag: should never happen
				throw new RuntimeException( "has-stackTag | no--capTag: should never happen" );
				
			case 3: // has-stackTag | has-capTag: {deserialized from local storage}
				// Remove "Parent" tag to prevent repeat deserialization
				final NBTTagCompound nbt = capTag.getCompoundTag( "Parent" );
				capTag.removeTag( "Parent" );
				
				final IModifiable primary2 = type.deserializeContexted( nbt );
				primary2.updatePrimaryState();
				return new ModuleWrapper( stack::getTagCompound, primary2 );
				// See case 1
				
			default: throw new RuntimeException( "Impossible to reach here" );
			}
		}
		
		@Override
		public void readNBTShareTag( ItemStack stack, @Nullable NBTTagCompound nbt ) {
			ModifiableItemType.this.getContexted( stack ).onReadNBTShareTag( nbt );
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
	
	/// Used for module render ///
	// TODO: maybe null on server side
//	protected static final FloatBuffer MAT_BUF = BufferUtils.createFloatBuffer( 16 );
	
	protected abstract class ModifiableItem extends Modifiable implements IItem
	{
		/**
		 * It will be fixed hierarchy position at {@link Side#SERVER}. Mixed with output of
		 * {@link IAnimator} on {@link Side#CLIENT} for rendering.
		 */
		// TODO: proper handle on server side
		protected transient final Mat4f mat = new Mat4f(); // FIXME: how to handle with this?
		
		/**
		 * @see Modifiable#Modifiable()
		 */
		protected ModifiableItem() { }
		
		/**
		 * @see Modifiable#Modifiable(NBTTagCompound)
		 */
		protected ModifiableItem( NBTTagCompound nbtToBeInit ) { super( nbtToBeInit ); }
		
		@Override
		public String name() { return ModifiableItemType.this.name; }
		
		@Override
		public String category() { return ModifiableItemType.this.category; }
		
		@Override
		public ItemStack toStack()
		{
			final ItemStack stack = new ItemStack( ModifiableItemType.this.item );
			final IModifiable module = ModifiableItemType.this.getContexted( stack );
			module.base().install( 0, this );
			module.syncNBTData();
			
			stack.setItemDamage( this.paintjob );
			return stack;
		}
		
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
			switch( key.name() )
			{
			case Key.TOGGLE_MODIFY:
			case Key.CO_TOGGLE_MODIFY:
				// TODO: maybe get modify op from protected method
				final PlayerPatchClient patch = PlayerPatchClient.instance;
				if( patch.operating() instanceof OpModifyClient )
					patch.toggleOperating();
				else patch.tryLaunch( OP_MODIFY.reset( this ) );
				break;
			}
			
			// For keys of category modify, just send to operation to handle them
			if( key.category().equals( KeyCategory.MODIFY ) )
				OP_MODIFY.handleKeyInput( key );
		}
		
		@Override
		public void applyTransform( int slot, IModifiable module, Mat4f dst )
		{
			dst.mul( this.mat );
			ModifiableItemType.this.slots.get( slot ).applyTransform( module, dst );
		}
		
		@SideOnly( Side.CLIENT )
		public void prepareHandRender(
			Collection< IDeferredRenderer > renderQueue0,
			Collection< IDeferredPriorityRenderer > renderQueue1,
			IAnimator animator
		) {
			// TODO: May not be necessary to call this every time if no item transform applied
			this.mat.setIdentity();
			this.base.applyTransform( this.baseSlot, this, this.mat );
			
			// TODO: maybe avoid instantiation to improve performance?
			ModifiableItemType.this.renderer.prepareHandRender(
				this.self(),
				this.wrapperAnimator( animator ),
				renderQueue0,
				renderQueue1
			);
			
			this.installed.forEach(
				mod -> mod.prepareHandRender( renderQueue0, renderQueue1, animator )
			);
		}
		
		@SideOnly( Side.CLIENT )
		public void prepareRender(
			Collection< IDeferredRenderer > renderQueue0,
			Collection< IDeferredPriorityRenderer > renderQueue1,
			IAnimator animator
		) {
			// TODO: May not be necessary to call this every time if no item transform applied
			this.mat.setIdentity();
			this.base.applyTransform( this.baseSlot, this, this.mat );
			
			// TODO: maybe avoid instantiation to improve performance?
			ModifiableItemType.this.renderer.prepareRender(
				this.self(),
				this.wrapperAnimator( animator ),
				renderQueue0,
				renderQueue1
			);
			
			this.installed.forEach(
				mod -> mod.prepareRender( renderQueue0, renderQueue1, animator )
			);
		}
		
		@Override
		@SideOnly( Side.CLIENT )
		public IModifiable newModifyIndicator()
		{
			return IModifiableType.REGISTRY.get( ModifiableItemType.this.modifyIndicator )
				.newContexted( new NBTTagCompound() ); // TODO: maybe a buffer instance
		}
		
		@Override
		@SideOnly( Side.CLIENT )
		public ResourceLocation texture() {
			return ModifiableItemType.this.paintjobs.get( this.paintjob ).texture();
		}
		
		@Override
		public String toString() { return ModifiableItemType.this.toString(); }
		
		@Override
		protected int id() { return Item.getIdFromItem( ModifiableItemType.this.item ); }
		
		@Override
		protected IModifiableType fromId( int id ) {
			return ( IModifiableType ) ( ( IItemTypeHost ) Item.getItemById( id ) ).meta();
		}
		
		@SideOnly( Side.CLIENT )
		protected IAnimator wrapperAnimator( IAnimator animator )
		{
			return ( channel, smoother, dst ) -> {
				switch( channel )
				{
				case IModifiableRenderer.CHANNEL_INSTALL:
					dst.mul( this.mat );
					break;
					
				default: animator.applyChannel( channel, smoother, dst );
				}
			};
		}
		
		@SuppressWarnings( "unchecked" )
		protected C self() { return ( C ) this; }
	}
}
