package com.fmum.common.item;

import javax.annotation.Nullable;

import com.fmum.client.item.IItemModel;
import com.fmum.common.FMUM;
import com.fmum.common.load.IContentProvider;
import com.fmum.common.load.IPostLoadSubscriber;
import com.fmum.common.load.RenderableMeta;
import com.fmum.common.meta.IMeta;
import com.fmum.common.tab.ICreativeTab;
import com.google.gson.annotations.SerializedName;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

public abstract class ItemType< C extends IItem, M extends IItemModel< ? > >
	extends RenderableMeta< M > implements IItemType, IPostLoadSubscriber
{
	/**
	 * For contexted item.
	 */
	@CapabilityInject( IItem.class )
	public static final Capability< IItem > CAPABILITY = null;
	
	protected transient Item item;
	
	protected String description = "fmum.description.missing";
	
	@SerializedName( value = "creativeTab", alternate = "itemGroup" )
	protected String creativeTab = FMUM.DEFAULT_TAB.name();
	
	@Override
	public IMeta build( String name, IContentProvider provider )
	{
		super.build( name, provider );
		
		IItemType.REGISTRY.regis( this );
		
		this.item = this.createItem();
		provider.regisPostLoadSubscriber( this );
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
	
	// TODO: may not be necessary
	@Override
	@SuppressWarnings( "unchecked" )
	public C getContexted( ItemStack stack ) {
		return ( C ) stack.getCapability( CAPABILITY, null );
	}
	
	protected abstract Item createItem();
	
	protected void setupCreativeTab()
	{
		final ICreativeTab tab = ICreativeTab.REGISTRY.getOrElse( this.creativeTab, () -> {
			this.logError( "fmum.fail_to_find_tab", this, this.creativeTab );
			return FMUM.DEFAULT_TAB;
		} );
		this.item.setCreativeTab( tab.creativeTab() );
		tab.itemSettledIn( this );
		
		// No longer needed, release it.
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
	}
}
