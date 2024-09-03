package com.fmum.item;

import com.fmum.load.BuildableType;
import com.fmum.load.IContentBuildContext;
import com.fmum.load.IPostLoadContext;
import com.google.gson.JsonObject;
import com.google.gson.annotations.Expose;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

public abstract class ItemType extends BuildableType implements IItemType
{
	@Expose
	protected String creative_tab;
	
	@Override
	public void build( JsonObject data, String fallback_name, IContentBuildContext ctx )
	{
		super.build( data, fallback_name, ctx );
		
		// Item creative tab may not be loaded yet, so we defer it to post load.
		ctx.regisPostLoadCallback( this::_checkCreativeTab );
	}
	
	protected void _checkCreativeTab( IPostLoadContext ctx )
	{
		if ( this.creative_tab == null ) {
			this.creative_tab = ctx.getFallbackCreativeTab();
		}
		// FIXME
//		else if (
//			!CreativeTabsUtil.lookupTab( this.creative_tab ).isPresent()
//			&& !this.creative_tab.equals( NO_CREATIVE_TAB )
//		) {
//			FMUM.LOGGER.error( "fmum.item_tab_not_found", this, this.creative_tab );
//			this.creative_tab = ctx.getFallbackCreativeTab();
//		}
	}
	
	@Override
	public void addCreativeTabItems( CreativeTabs tab, NonNullList< ItemStack > items )
	{
		if ( tab.getTabLabel().equals( this.creative_tab ) )
		{
			final short meta = 0;
			final ItemStack stack = this.newItemStack( meta );
			items.add( stack );
		}
	}
	
	@Override
	public String getName() {
		return this.name;
	}
}
