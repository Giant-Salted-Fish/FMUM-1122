package com.mcwb.common.item;

import javax.annotation.Nullable;

import com.google.gson.annotations.SerializedName;
import com.mcwb.client.item.IItemRenderer;
import com.mcwb.common.MCWB;
import com.mcwb.common.load.IRequirePostLoad;
import com.mcwb.common.load.RenderableMeta;
import com.mcwb.common.meta.IMeta;
import com.mcwb.common.pack.IContentProvider;
import com.mcwb.common.tab.ICreativeTab;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;

public abstract class ItemMeta< C extends IContextedItem, M extends IItemRenderer< ? super C > >
	extends RenderableMeta< M > implements IItemMeta, IRequirePostLoad
{
	protected transient Item item;
	
	protected String description = "mcwb.desription_missing";
	
	@SerializedName( value = "creativeTab", alternate = "tab" )
	protected String creativeTab = MCWB.DEF_TAB.name();
	
	@Override
	public IMeta build( String name, IContentProvider provider )
	{
		super.build( name, provider );
		
		// Setup corresponding item
		this.item = this.createItem();
		
		// Successfully setup the item, register itself
		IItemMeta.REGISTRY.regis( this );
		provider.regisPostLoad( this );
		return this;
	}
	
	@Override
	public void onPostLoad()
	{
		// Locate creative tab and set it for item here as the customized creative may not be \
		// loaded right after the parse of the type item
		this.setupCreativeTab();
	}
	
	@Override
	public Item item() { return this.item; }
	
	@Override
	public C getContexted( ICapabilityProvider provider ) {
		return provider.getCapability( this.capability(), null );
	}
	
	protected abstract Item createItem();
	
	// TODO: maybe remove this
	protected abstract Capability< ? extends C > capability();
	
	protected void setupCreativeTab()
	{
		// Try to locate required creative tab
		final ICreativeTab tab = ICreativeTab.REGISTRY.getOrElse(
			this.creativeTab,
			() -> {
				this.error( "mcwb.fail_to_find_tab", this, this.creativeTab );
				return MCWB.DEF_TAB;
			}
		);
		this.item.setCreativeTab( tab.creativeTab() );
		tab.itemSettledIn( this );
		
		// No longer needed, release it
		this.creativeTab = null;
	}
	
	protected abstract class ItemBase extends Item implements IItemMetaHost
	{
		protected ItemBase( int maxStackSize, int maxDamage )
		{
			// Setup basic info for this item
			this.setRegistryName( ItemMeta.this.name );
			this.setTranslationKey( ItemMeta.this.name );
			this.setMaxStackSize( maxStackSize );
			this.setMaxDamage( maxDamage );
		}
		
		@Override
		public IItemMeta meta() { return ItemMeta.this; }
		
		@Override
		public abstract ICapabilityProvider initCapabilities(
			ItemStack stack,
			@Nullable NBTTagCompound capTag
		);
		
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
		
		@Override
		public void onUpdate(
			ItemStack stack,
			World worldIn,
			Entity entityIn,
			int itemSlot,
			boolean isSelected
		) {
			// TODO: maybe move to context? // TODO: check hand and entity
			if( worldIn.isRemote && isSelected )
				ItemMeta.this.model.onLogicTick(
					ItemMeta.this.getContexted( stack ),
					EnumHand.MAIN_HAND
				);
		}
	}
	
	/**
	 * TODO: proper intro
	 * 
	 * @author Giant_Salted_Fish
	 */
	protected abstract class HackedCapItem extends ItemBase
	{
		protected HackedCapItem( int maxStackSize, int maxDamage ) {
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
				final HackedNBTTagCompound hackedTag = new HackedNBTTagCompound();
				final ICapabilityProvider provider = this.newInitedCap( hackedTag );
				stack.setTagCompound( hackedTag );
				return provider;
				
			case 1: // no--stackTag | has-capTag: {copy stack}
				// As #serializeNBT() method will actually return the stack tag of the copy \
				// target, hence copy the corresponding tag in given capTag and set it as the copy \
				// delegate to ensure that the new stack will have the same tag as its compound \
				// tag. See ItemStack#copy().
				final NBTTagCompound oriStackTag = capTag.getCompoundTag( "Parent" );
				final HackedNBTTagCompound copied = new HackedNBTTagCompound( oriStackTag.copy() );
				
				// This is necessary #deserializeNBT(NBTTagCompound) will later be called on this \
				// Capability on this given capTag. Hence replace it to make sure that we bind \
				// context to the hacked tag. See ItemStack#forgeInit().
				capTag.setTag( "Parent", copied );
				
				// This is necessary as the stack tag of this stack will later be set to the \
				// instance returned by NBTTagCompound#copy() Call on the stack tag of the copy \
				// target. And the oriStackTag is exactly the stack tag of the copy target. Hence \
				// just set the copy delegate to make sure the stack tag will be the same as the \
				// bounden capability tag.
				( ( HackedNBTTagCompound ) oriStackTag ).setCopyDelegate( copied );
				
				return this.newRawCap();
				
			case 2: // has-stackTag | no--capTag: should never happen
				throw new RuntimeException( "has-stackTag | no--capTag: should never happen" );
				
			case 3: // has-stackTag | has-capTag: {deserialized from local storage}
//				if( stackTag.getClass() == HackedNBTTagCompound.class )
//				{
//					MCWB.MOD.error( "=====" ); // TODO: check if it is not hacked
//				}
				// Handled similar to the case 1
				final HackedNBTTagCompound hackedTag1 = new HackedNBTTagCompound( stackTag );
				capTag.setTag( "Parent", hackedTag1 );
				stack.setTagCompound( hackedTag1 );
				return this.newRawCap();
				
			default: throw new RuntimeException( "Impossible to reach here" );
			}
		}
		
		@Override
		public void readNBTShareTag( ItemStack stack, @Nullable NBTTagCompound nbt )
		{
			// Called on network packet stack creation. At this time a context has been created \
			// and setup. Hence load the state from the stack tag into its context.
			final HackedNBTTagCompound hackedTag = new HackedNBTTagCompound( nbt );
			this.getContexted( stack ).deserializeNBT( hackedTag );
			stack.setTagCompound( hackedTag );
		}
		
		protected abstract ICapabilityProvider newInitedCap( NBTTagCompound nbt );
		
		protected abstract ICapabilityProvider newRawCap();
		
		protected abstract INBTSerializable< NBTTagCompound > getContexted( ItemStack stack );
	}
}
