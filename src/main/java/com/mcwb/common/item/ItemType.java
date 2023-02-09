package com.mcwb.common.item;

import javax.annotation.Nullable;

import com.google.gson.annotations.SerializedName;
import com.mcwb.client.item.IItemRenderer;
import com.mcwb.common.MCWB;
import com.mcwb.common.load.IRequirePostLoad;
import com.mcwb.common.load.RenderableMeta;
import com.mcwb.common.meta.IContexted;
import com.mcwb.common.meta.IMeta;
import com.mcwb.common.pack.IContentProvider;
import com.mcwb.common.tab.ICreativeTab;

import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;

public abstract class ItemType< C extends IItem, M extends IItemRenderer< ? super C > >
	extends RenderableMeta< M > implements IItemType, IRequirePostLoad
{
	protected transient Item item;
	
	protected String description = "mcwb.description.missing";
	
	@SerializedName( value = "creativeTab", alternate = "tab" )
	protected String creativeTab = MCWB.DEF_TAB.name();
	
	@Override
	public IMeta build( String name, IContentProvider provider )
	{
		super.build( name, provider );
		
		// Setup corresponding item
		this.item = this.createItem();
		
		// Successfully setup the item, register itself
		IItemType.REGISTRY.regis( this );
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
	@SuppressWarnings( "unchecked" )
	public C getContexted( ICapabilityProvider provider ) {
		return ( C ) provider.getCapability( IContexted.CAPABILITY, null );
	}
	
	protected abstract Item createItem();
	
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
	
	protected abstract class VanillaItem extends Item implements IItemTypeHost
	{
		protected VanillaItem( int maxStackSize, int maxDamage )
		{
			// Setup basic info for this item
			this.setRegistryName( ItemType.this.name );
			this.setTranslationKey( ItemType.this.name );
			this.setMaxStackSize( maxStackSize );
			this.setMaxDamage( maxDamage );
		}
		
		@Override
		public IItemType meta() { return ItemType.this; }
		
		@Override
		public abstract ICapabilityProvider initCapabilities(
			ItemStack stack,
			@Nullable NBTTagCompound capTag
		);
		
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
				ItemType.this.model.onLogicTick(
					ItemType.this.getContexted( stack ),
					EnumHand.MAIN_HAND
				);
		}
	}
}
