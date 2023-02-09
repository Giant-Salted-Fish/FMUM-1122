package com.mcwb.common.item;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

import com.google.gson.annotations.SerializedName;
import com.mcwb.client.IAutowireBindTexture;
import com.mcwb.client.MCWBClient;
import com.mcwb.client.input.IKeyBind;
import com.mcwb.client.input.Key;
import com.mcwb.client.item.IItemRenderer;
import com.mcwb.client.modify.IModifiableRenderer;
import com.mcwb.client.modify.IMultPassRenderer;
import com.mcwb.client.player.ModifyOperationClient;
import com.mcwb.client.player.PlayerPatchClient;
import com.mcwb.client.render.IAnimator;
import com.mcwb.client.render.Renderer;
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
	public static final ModifyOperationClient MODIFY_OP; static {
		MODIFY_OP = MCWB.MOD.isClient() ? new ModifyOperationClient() : null;
	}
	
	@SerializedName( value = "category", alternate = "group" )
	protected String category;
	
	@SerializedName( value = "paramScale", alternate = "scale" )
	protected float paramScale = 1F;
	
	@SerializedName( value = "slots", alternate = "moduleSlots" )
	protected List< IModuleSlot > slots = Collections.emptyList();
	
	@SerializedName( value = "snapshot", alternate = "preInstalls" )
	protected IModuleSnapshot snapshot = ModuleSnapshot.DEFAULT;
	
	@SerializedName( value = "paintjobs", alternate = "skins" )
	protected List< IPaintjob > paintjobs = Collections.emptyList();
	
	@SideOnly( Side.CLIENT )
	protected String modifyIndicator = "modify_indicator"; // TODO: make it the default indicator provided by MCWB
	
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
				// TODO: set a default indicator this.modifyIndicator = ;
				this.error( "mcwb.fail_to_find_indicator", this, this.modifyIndicator );
			}
		} );
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
			switch( ( stackTag != null ? 2 : 0 ) + ( capTag != null ? 1 : 0 ) )
			{
			case 0: // no--stackTag | no--capTag: {create stack}, {deserialize from network packet}
				final IModifiable contexted = ModifiableItemType
					.this.newContexted( new NBTTagCompound() );
				ModifiableItemType.this.snapshot.initContexted( name -> contexted );
				
				stack.setTagCompound( new NBTTagCompound() );
				final ModuleWrapper wrapper = new ModuleWrapper( stack::getTagCompound, contexted );
				wrapper.syncNBTData();
				return wrapper;
				
			case 1: // no--stackTag | has-capTag: {copy stack}
				// A little bit more work to do to handle copy case as the #serializeNBT() of the \
				// module could be its bounden tag, hence copy it before deserialize
				final NBTTagCompound copyiedTag = capTag.getCompoundTag( "Parent" ).copy();
				capTag.removeTag( "Parent" );
				return new ModuleWrapper(
					stack::getTagCompound,
					ModifiableItemType.this.deserializeContexted( copyiedTag )
				);
				// #syncNBTData() not called as it is possible that the stack tag has not been set \
				// yet. This will not cause problem because the stack tag also has full context.
				
			case 2: // has-stackTag | no--capTag: should never happen
				throw new RuntimeException( "has-stackTag | no--capTag: should never happen" );
				
			case 3: // has-stackTag | has-capTag: {deserialized from local storage}
				// Remove "Parent" tag to prevent repeat deserialization
				final NBTTagCompound nbt = capTag.getCompoundTag( "Parent" );
				capTag.removeTag( "Parent" );
				return new ModuleWrapper(
					stack::getTagCompound,
					ModifiableItemType.this.deserializeContexted( nbt )
				);
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
	protected static final FloatBuffer MAT_BUF = BufferUtils.createFloatBuffer( 16 );
	
	protected abstract class ModifiableItem extends Modifiable
		implements IItem, IAutowireBindTexture
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
			module.deserializeNBT( this.nbt );
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
		public boolean onHandRender( EnumHand hand ) {
			return ModifiableItemType.this.model.onHandRender( this.self(), hand );
		}
		
		@Override
		@SideOnly( Side.CLIENT )
		public boolean onSpecificHandRender( EnumHand hand ) {
			return ModifiableItemType.this.model.onSpecificHandRender( this.self(), hand );
		}
		
		@Override
		@SideOnly( Side.CLIENT )
		public void onRenderTick( EnumHand hand ) {
			ModifiableItemType.this.model.onRenderTick( this.self(), hand );
		}
		
		@Override
		@SideOnly( Side.CLIENT )
		public void onKeyInput( IKeyBind key )
		{
			switch( key.name() )
			{
			case Key.TOGGLE_MODIFY:
			case Key.CO_TOGGLE_MODIFY:
				// TODO: maybe get modify op from protected method
				final PlayerPatchClient patch = PlayerPatchClient.instance;
				if( patch.operating() instanceof ModifyOperationClient )
					patch.toggleOperating();
				else patch.tryLaunch( MODIFY_OP.reset() );
				break;
			}
			
			// For keys of category modify, just send to operation to handle them
			if( key.category().equals( Key.CATEGORY_MODIFY ) )
				MODIFY_OP.handleKeyInput( key );
		}
		
		@Override
		public void applyTransform( int slot, IModifiable module, Mat4f dst )
		{
			Mat4f.mul( this.mat, dst, dst );
			ModifiableItemType.this.slots.get( slot ).applyTransform( module, dst );
		}
		
		@Override
		@SideOnly( Side.CLIENT )
		public void prepareRenderer(
			Collection< IMultPassRenderer > renderQueue,
			IAnimator animator
		) {
			this.mat.setIdentity();
			this.base.applyTransform( this.baseSlot, this, this.mat );
			
			// TODO: maybe avoid instantiation to improve performance?
			renderQueue.add( queue -> {
				// Apply transform before actual render
				final FloatBuffer buf = MAT_BUF;
				buf.clear();
				this.mat.store( buf );
				buf.flip();
				GL11.glMultMatrix( buf );
				
				// Bind texture and render!
				switch( this.modifyState )
				{
				case SELECTED_OK:
				case SELECTED_CONFLICT:
					final boolean ok = this.modifyState == ModifyState.SELECTED_OK;
					this.bindTexture( ok ? Renderer.TEXTURE_GREEN : Renderer.TEXTURE_RED );
					break;
				default:
					final List< IPaintjob > paintjobs = ModifiableItemType.this.paintjobs;
					MCWBClient.MOD.bindTexture( paintjobs.get( this.paintjob ).texture() );
				}
				ModifiableItemType.this.model.renderModule( this.self(), animator, renderQueue );
			} );
			
			this.installed.forEach( mod -> mod.prepareRenderer( renderQueue, animator ) );
		}
		
//		@Override
//		@SideOnly( Side.CLIENT )
//		public IModifiable newModifyDelegate() {
//			return ModifiableItemType.this.deserializeContexted( this.nbt.copy() );
//		}
		
		@Override
		@SideOnly( Side.CLIENT )
		public IModifiable newModifyIndicator()
		{
			return IModifiableType.REGISTRY.get( ModifiableItemType.this.modifyIndicator )
				.newContexted( new NBTTagCompound() ); // TODO: maybe a buffer instance
		}
		
		@Override
		public String toString() { return ModifiableItemType.this.toString(); }
		
		@Override
		protected int id() { return Item.getIdFromItem( ModifiableItemType.this.item ); }
		
		@Override
		protected IModifiableType fromId( int id ) {
			return ( IModifiableType ) ( ( IItemTypeHost ) Item.getItemById( id ) ).meta();
		}
		
		@SuppressWarnings( "unchecked" )
		protected C self() { return ( C ) this; }
	}
}
