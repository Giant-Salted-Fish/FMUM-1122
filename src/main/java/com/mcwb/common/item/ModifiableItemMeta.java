package com.mcwb.common.item;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

import com.google.gson.JsonDeserializer;
import com.google.gson.annotations.SerializedName;
import com.mcwb.client.MCWBClient;
import com.mcwb.client.input.IKeyBind;
import com.mcwb.client.input.Key;
import com.mcwb.client.item.IItemModel;
import com.mcwb.client.item.ItemAnimatorState;
import com.mcwb.client.modify.IModifiableModel;
import com.mcwb.client.modify.IMultPassRenderer;
import com.mcwb.client.render.IAnimator;
import com.mcwb.common.MCWB;
import com.mcwb.common.meta.IMeta;
import com.mcwb.common.modify.IContextedModifiable;
import com.mcwb.common.modify.IModifiableMeta;
import com.mcwb.common.modify.IModuleSlot;
import com.mcwb.common.modify.IModuleSnapshot;
import com.mcwb.common.modify.ModuleSnapshot;
import com.mcwb.common.modify.RailSlot;
import com.mcwb.common.pack.IContentProvider;
import com.mcwb.common.paintjob.IPaintjob;
import com.mcwb.common.paintjob.Paintjob;
import com.mcwb.util.Mat4f;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public abstract class ModifiableItemMeta<
	C extends IContextedItem & IContextedModifiable,
	M extends IItemModel< ? super C > & IModifiableModel< ? super C >
> extends CategoriedItemType< C, M > implements IModifiableMeta, IPaintjob
{
	public static final JsonDeserializer< IModuleSlot >
		SLOT_ADAPTER = ( json, typeOfT, context ) -> MCWB.GSON.fromJson( json, RailSlot.class );
	
	public static final JsonDeserializer< IModuleSnapshot >
		MODULE_SNAPSHOT_ADAPTER = ( json, typeOfT, context ) -> MCWB.GSON
			.fromJson( json, ModuleSnapshot.class );
	
	public static final JsonDeserializer< IPaintjob >
		PAINTJOB_ADAPTER = ( json, typeOfT, context ) -> MCWB.GSON.fromJson( json, Paintjob.class );
	
	// TODO: test if this crash on server side
//	public static final ModifyOp< ModifiableItemType< ?, ?, ? >.ModifiableItemCtx >
//		MODIFY_OP = MCWB.MOD.isClient() ? new ModifyOp<>() : null;
	
	@SerializedName( value = "paramScale", alternate = "scale" )
	protected float paramScale = 1F;
	
	@SerializedName( value = "slots", alternate = "moduleSlots" )
	protected List< IModuleSlot > slots = Collections.emptyList();
	
	@SerializedName( value = "snapshot", alternate = "preInstalls" )
	protected IModuleSnapshot snapshot = ModuleSnapshot.DEFAULT;
	
	@SerializedName( value = "paintjobs", alternate = "skins" )
	protected List< IPaintjob > paintjobs = Collections.emptyList();
	
	@Override
	public IMeta build( String name, IContentProvider provider )
	{
		super.build( name, provider );
		
		IModifiableMeta.REGISTRY.regis( this );
		
		// Add itself as the default paintjob
		if( this.paintjobs.size() == 0 )
			this.paintjobs = new ArrayList<>();
		this.paintjobs.add( 0, this );
		
		// Apply model scale
		this.slots.forEach( s -> s.scale( this.paramScale ) );
		// TODO: hit boxes
		return this;
	}
	
	@Override
	public IContextedModifiable newContexted( NBTTagCompound nbt ) { return this.newCtxed( nbt ); }
	
	@Override
	public IModuleSlot getSlot( int idx ) { return this.slots.get( idx ); }
	
	@Override
	public int slotCount() { return this.slots.size(); }
	
	@Override
	public int paintjobCount() { return this.paintjobs.size(); }
	
	@Override
	public void injectPaintjob( IPaintjob paintjob ) { this.paintjobs.add( paintjob ); }
	
	@Override
	@SideOnly( Side.CLIENT )
	public ResourceLocation texture() { return this.texture; }
	
	// TODO: handle paintjobs
	@Override
	protected Item createItem() { return this.new ModifiableItem( 1, 0 ); }
	
	/**
	 * Purely for convenience. Change every place this is used if you do not want to use
	 * {@link ContextedModifiableItem}.
	 */
	protected abstract ContextedModifiableItem newCtxed( NBTTagCompound nbt );
	
	protected class ModifiableItem extends ItemBase
	{
		protected ModifiableItem( int maxStackSize, int maxDamage ) {
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
			switch( ( stackTag != null ? 2 : 0 ) + ( capTag != null ? 1 : 0 ) )
			{
			case 0: // no--stackTag | no--capTag: {create stack}, {deserialize from network packet}
				// We actually can not distinguish from these two cases. But in general it will \
				// have correct behavior by just creating a default context and set the tag of the \
				// stack to the bounden tag of the context. This works for network packet \
				// deserialize as it will later call data deserialize to parse data in NBT tag. \
				// See below #readNBTShareTag(ItemStack, NBTTagCompound)
				final HackedNBTTagCompound hackTag = new HackedNBTTagCompound();
				final ICapabilityProvider pvd = this.newCap( hackTag );
				ModifiableItemMeta.this.snapshot.initContexted( ( name, nbt ) -> pvd
					.getCapability( ModifiableItemMeta.this.capability(), null ) );
				stack.setTagCompound( hackTag );
				return pvd;
				
			case 1: // no--stackTag | has-capTag: {copy stack}
				// As #serialize() method will actually return the stack tag of then copy target, \
				// hence copy the capTag and set it as the copy delegate to make sure that the new \
				// stack will have the same tag instance as its compound tag. See ItemStack#copy().
				final HackedNBTTagCompound copied = new HackedNBTTagCompound( capTag.copy() );
				( ( HackedNBTTagCompound ) capTag ).setCopyDelegate( copied );
				return this.copyCap( copied );
				
			case 2: // has-stackTag | no--capTag: should never happen
				throw new RuntimeException( "has-stackTag | no--capTag: should never happen" );
				
			case 3: // has-stackTag | has-capTag: {deserialized from local storage}
				return this.copyCap( new HackedNBTTagCompound( stackTag ) );
				
			default: throw new RuntimeException( "Impossible to reach here" );
			}
		}
		
		@Override
		public void readNBTShareTag( ItemStack stack, @Nullable NBTTagCompound nbt )
		{
			stack.setTagCompound( nbt );
			
			// Called on network packet stack creation. At this time a context has been created \
			// with default snapshot. Here reset the context with the new NBT data from the stack \
			// tag.
			final IContextedModifiable mod = ModifiableItemMeta.this.getContexted( stack );
			mod.deserializeNBT( new HackedNBTTagCompound( nbt ) );
		}
		
		protected ICapabilityProvider copyCap( NBTTagCompound from )
		{
			// TODO: static final NBT instance
			final ICapabilityProvider pvd = ModifiableItemMeta.this.newCtxed(
				new NBTTagCompound()
			);
			pvd.getCapability( ModifiableItemMeta.this.capability(), null ).deserializeNBT( from );
			return pvd;
		}
		
		protected ICapabilityProvider newCap( NBTTagCompound nbt ) {
			return ModifiableItemMeta.this.newCtxed( nbt );
		}
	}
	
	/// Used for module render ///
	// TODO: maybe null on server side
	protected static final FloatBuffer MAT_BUF = BufferUtils.createFloatBuffer( 16 );
	
	protected abstract class ContextedModifiableItem
		implements IContextedItem, IContextedModifiable, ICapabilityProvider
	{
		protected static final String DATA_TAG = "d";
		protected static final String MOD_TAG = "m";
		
		/// Data that is not persistent ///
		/**
		 * It will be fixed hierarchy position at {@link Side#SERVER}. Mixed with output of
		 * {@link IAnimator} on {@link Side#CLIENT} for rendering.
		 */
		// TODO: proper handle on server side
		protected transient final Mat4f globalMat = new Mat4f(); // FIXME: how to handle with this?
		
		protected transient ModifyState modifyState = ModifyState.NOT_SELECTED;
		
		protected transient IContextedModifiable base = null;
		protected transient short baseSlot = 0;
		
		/**
		 * Bounden NBT used for data persistence
		 */
		protected transient NBTTagCompound nbt;
		
		/// Persistent data ///
		protected short paintjob = 0;
		
		protected final ArrayList< IContextedModifiable > installed = new ArrayList<>();
		protected final byte[] indices = new byte[ ModifiableItemMeta.this.slots.size() ];
		
		/**
		 * Create a new context that is not initialized. You must call
		 * {@link #deserializeNBT(NBTTagCompound)} to setup this context before you actual use it.
		 */
		protected ContextedModifiableItem() { }
		
		/**
		 * Setup the given NBT tag and bind to it
		 * 
		 * @param nbt A clean compound NBT that needed to be setup
		 */
		protected ContextedModifiableItem( NBTTagCompound nbt )
		{
			final int[] data = new int[ this.dataSize() ];
			data[ 0 ] = Item.getIdFromItem( ModifiableItemMeta.this.item );
			nbt.setIntArray( DATA_TAG, data );
			nbt.setTag( MOD_TAG, new NBTTagList() );
			
			this.nbt = nbt;
		}
		
		@Override
		public boolean hasCapability( Capability< ? > capability, @Nullable EnumFacing facing ) {
			return capability == CAPABILITY;
		}
		
		@Override
		public < T > T getCapability( Capability< T > capability, @Nullable EnumFacing facing ) {
			return CAPABILITY.cast( this );
		}
		
		@Override
		public IModifiableMeta meta() { return ModifiableItemMeta.this; }
		
		@Override
		public IContextedModifiable base() { return this.base; }
		
		@Override
		public int baseSlot() { return this.baseSlot; }
		
		@Override
		public void forEach( Consumer< IContextedModifiable > visitor )
		{
			visitor.accept( this );
			this.installed.forEach( mod -> visitor.accept( mod ) );
		}
		
		@Override
		public void install( int slot, IContextedModifiable module )
		{
			final IContextedModifiable target = module.onBeingInstalled( this, slot );
			final int idx = this.getIdx( slot + 1 );
			
			// Update installed list
			this.installed.add( idx, target );
			
			// Update NBT tag
			final NBTTagList modList = this.nbt.getTagList( MOD_TAG, NBT.TAG_COMPOUND );
			final NBTTagCompound tarTag = target.serializeNBT();
			modList.appendTag( tarTag );
			for( int i = modList.tagCount(); i-- > idx; modList.set( i + 1, modList.get( i ) ) );
			modList.set( idx, tarTag );
			
			// Update indices
			final int[] data = this.nbt.getIntArray( DATA_TAG );
			while( slot++ < this.indices.length )
			{
				final int val = 1 + this.getIdx( slot );
				this.setIdx( slot, val );
				this.setIdx( data, slot, val );
			}
		}
		
		@Override
		public IContextedModifiable remove( int slot, int idx )
		{
			// Update installed list
			final int tarIdx = this.getIdx( slot ) + idx;
			final IContextedModifiable target = this.installed.remove( tarIdx );
			
			// Update nbt tag
			final NBTTagList modList = this.nbt.getTagList( MOD_TAG, NBT.TAG_COMPOUND );
			modList.removeTag( tarIdx );
			
			// Update indices
			final int[] data = this.nbt.getIntArray( DATA_TAG );
			while( slot++ < this.indices.length )
			{
				final int val = -1 + this.getIdx( slot );
				this.setIdx( slot, val );
				this.setIdx( data, slot, val );
			}
			return target.onBeingRemoved();
		}
		
		@Override
		public void setBase( IContextedModifiable base, int baseSlot )
		{
			this.base = base;
			this.baseSlot = ( short ) baseSlot;
			
			// Update position for server side. Client side will always update matrix before render.
			// FIXME: matrix is not serialized hence this will not work on server side
//			this.globalMat.setIdentity();
//			base.applySlotTransform( this, this.globalMat );
		}
		
		@Override
		public IContextedModifiable onBeingRemoved()
		{
			this.base = null; // TODO: maybe a default base
			this.baseSlot = -1; // TODO: maye reset to 0?
			return this;
			
			// Update position for server side
//			this.globalMat.setIdentity();
		}
		
		@Override
		public IContextedModifiable getInstalled( int slot, int idx ) {
			return this.installed.get( this.getIdx( slot ) + idx );
		}
		
		@Override
		public int getInstalledCount( int slot ) {
			return this.getIdx( slot + 1 ) - this.getIdx( slot );
		}
		
		@Override
		public int paintjob() { return this.paintjob; }
		
		@Override
		public void $paintjob( int paintjob )
		{
			this.paintjob = ( short ) paintjob;
			final int[] data = this.nbt.getIntArray( DATA_TAG );
			data[ 0 ] &= 0xFFFF;
			data[ 0 ] |= paintjob << 16;
		}
		
		@Override
		public ModifyState modifyState() { return this.modifyState; }
		
		@Override
		public void $modifyState( ModifyState state ) { this.modifyState = state; }
		
		// TODO: maybe change to apply transform?
		@Override
		public void getSlotTransform( IContextedModifiable installed, Mat4f dst )
		{
			dst.load( this.globalMat );
			ModifiableItemMeta.this.slots.get( installed.baseSlot() )
				.applyTransform( installed, dst );
		}
		
		@Override
		@SideOnly( Side.CLIENT )
		public boolean onHandRender( EnumHand hand ) {
			return ModifiableItemMeta.this.model.onHandRender( this.self(), hand );
		}
		
		@Override
		@SideOnly( Side.CLIENT )
		public boolean onSpecificHandRender( EnumHand hand ) {
			return ModifiableItemMeta.this.model.onSpecificHandRender( this.self(), hand );
		}
		
		@Override
		@SideOnly( Side.CLIENT )
		public void onRenderTick( EnumHand hand ) {
			ModifiableItemMeta.this.model.onRenderTick( this.self(), hand );
		}
		
		@Override
		@SideOnly( Side.CLIENT )
		public void onKeyInput( IKeyBind key )
		{
			switch( key.name() )
			{
			case Key.TOGGLE_MODIFY:
			case Key.CO_TOGGLE_MODIFY:
				
				break;
			}
			
			// For keys of category modify, just send to operation to handle them
			if( key.category().equals( Key.CATEGORY_MODIFY ) )
				; // TODO
		}
		
		@Override
		@SideOnly( Side.CLIENT )
		public void prepareRenderer(
			Collection< IMultPassRenderer > renderQueue,
			IAnimator animator
		) {
			if( this.base == null ) // TODO: A default instance to avoid base check
			{
				this.globalMat.setIdentity();
				animator.applyChannel(
					ItemAnimatorState.ITEM, MCWBClient.MOD.smoother(), this.globalMat
				);
			}
			else this.base.getSlotTransform( this, this.globalMat );
			
			// TODO: maybe avoid instantiation to improve performance?
			renderQueue.add( queue -> {
				// Apply transform before actual render
				final FloatBuffer buf = MAT_BUF;
				buf.clear();
				this.globalMat.store( buf );
				buf.flip();
				GL11.glMultMatrix( buf );
				
				// Bind texture and render!
				MCWBClient.MOD.bindTexture( ModifiableItemMeta
					.this.paintjobs.get( this.paintjob ).texture() );
				ModifiableItemMeta.this.model.renderModule( this.self(), animator, renderQueue );
			} );
		}
		
		@Override
		public NBTTagCompound serializeNBT() { return this.nbt; }
		
		@Override
		public void deserializeNBT( NBTTagCompound nbt )
		{
			// Read paintjob
			final int data[] = nbt.getIntArray( DATA_TAG );
			this.paintjob = ( short ) ( data[ 0 ] >>> 16 );
			
			// Read install indices
			for( int i = this.indices.length; i > 0; --i )
				this.setIdx( i, this.getIdx( data, i ) );
			
			// Read installed modules
			final NBTTagList modList = nbt.getTagList( MOD_TAG, NBT.TAG_COMPOUND );
			for( int i = 0, size = modList.tagCount(), slot = 0; i < size; ++i )
			{
				final NBTTagCompound modTag = modList.getCompoundTagAt( i );
				final Item item = Item.getItemById( 0xFFFF & modTag.getIntArray( DATA_TAG )[ 0 ] );
				final IModifiableMeta meta = ( IModifiableMeta ) ( ( IItemMetaHost ) item ).meta();
				final IContextedModifiable module = meta.newContexted( new NBTTagCompound() ); // TODO: a default tag instance or refactor
				module.deserializeNBT( modTag );
				
				while( i >= this.getIdx( slot + 1 ) ) ++slot;
				module.setBase( this, slot );
				this.installed.add( module );
			}
			
			// Do not forget to bind to the given tag
			this.nbt = nbt;
		}
		
		protected final int getIdx( int slot ) {
			return slot > 0 ? 0xFF & this.indices[ slot - 1 ] : 0;
		}
		
		protected final void setIdx( int slot, int idx ) {
			this.indices[ slot - 1 ] = ( byte ) idx;
		}
		
		// TODO: if 0 is never used then just remove it
		protected final int getIdx( int[] data, int slot ) {
			return slot > 0 ? 0xFF & data[ 1 + ( slot - 1 ) / 4 ] >>> ( ( slot - 1 ) % 4 ) * 8 : 0;
		}
		
		protected final void setIdx( int[] data, int slot, int val )
		{
			final int idx = 1 + slot / 4;
			final int offset = ( slot % 4 ) * 8;
			data[ idx ] &= ~( 0xFF << offset );       // Clear value
			data[ idx ] |= ( 0xFF & val ) << offset;  // Set value
		}
		
		protected int dataSize() { return 1 + ( this.indices.length + 3 ) / 4; }
		
		protected abstract C self();
	}
}
