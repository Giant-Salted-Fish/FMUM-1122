package com.mcwb.common.item;

import javax.annotation.Nullable;

import com.google.gson.annotations.SerializedName;
import com.mcwb.client.item.IItemRenderer;
import com.mcwb.common.MCWB;
import com.mcwb.common.load.IContentProvider;
import com.mcwb.common.load.IPostLoadSubscriber;
import com.mcwb.common.load.RenderableMeta;
import com.mcwb.common.meta.IContexted;
import com.mcwb.common.meta.IMeta;
import com.mcwb.common.tab.ICreativeTab;

import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

public abstract class ItemType< C extends IItem, R extends IItemRenderer< ? super C > >
	extends RenderableMeta< R > implements IItemType, IPostLoadSubscriber
{
	protected transient Item item;
	
	protected String description = "mcwb.description.missing";
	
	@SerializedName( value = "creativeTab", alternate = "itemGroup" )
	protected String creativeTab = MCWB.DEFAULT_TAB.name();
	
	@Override
	public IMeta build( String name, IContentProvider provider )
	{
		super.build( name, provider );
		
		IItemType.REGISTRY.regis( this );
		
		this.item = this.createItem();
		provider.regis( this );
		return this;
	}
	
	@Override
	public void onPostLoad()
	{
		// We can not guarantee that creative tabs are loaded before the items. Hence locate its \
		// creative tab on post load.
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
		final ICreativeTab tab = ICreativeTab.REGISTRY.getOrElse( this.creativeTab, () -> {
			this.error( "mcwb.fail_to_find_tab", this, this.creativeTab );
			return MCWB.DEFAULT_TAB;
		} );
		this.item.setCreativeTab( tab.creativeTab() );
		tab.itemSettledIn( this );
		
		// No longer needed, release it
		this.creativeTab = null;
	}
	
	protected abstract class VanillaItem extends Item implements IItemTypeHost
	{
		protected VanillaItem( int maxStackSize, int maxDamage )
		{
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
				ItemType.this.renderer.tickInHand(
					ItemType.this.getContexted( stack ),
					EnumHand.MAIN_HAND
				);
		}
	}
}
