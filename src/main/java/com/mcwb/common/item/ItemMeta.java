package com.mcwb.common.item;

import javax.annotation.Nullable;

import com.google.gson.annotations.SerializedName;
import com.mcwb.client.item.IItemModel;
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

public abstract class ItemMeta< C extends IContextedItem, M extends IItemModel< ? super C > >
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
}
