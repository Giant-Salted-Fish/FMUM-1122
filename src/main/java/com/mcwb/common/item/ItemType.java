package com.mcwb.common.item;

import javax.annotation.Nullable;

import com.google.gson.annotations.SerializedName;
import com.mcwb.client.item.IItemModel;
import com.mcwb.common.MCWB;
import com.mcwb.common.load.IContentProvider;
import com.mcwb.common.load.IPostLoadSubscriber;
import com.mcwb.common.load.RenderableMeta;
import com.mcwb.common.meta.IMeta;
import com.mcwb.common.tab.ICreativeTab;

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
	@CapabilityInject( Object.class )
	public static final Capability< IItem > CAPABILITY = null;
	
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
			this.logError( "mcwb.fail_to_find_tab", this, this.creativeTab );
			return MCWB.DEFAULT_TAB;
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
