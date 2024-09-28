package com.fmum.item;

import com.fmum.FMUM;
import com.fmum.load.BuildableType;
import com.fmum.load.IContentBuildContext;
import com.fmum.load.IPostLoadContext;
import com.fmum.load.JsonData;
import com.fmum.tab.CreativeTabUtil;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public abstract class ItemType extends BuildableType implements IItemType
{
	protected String creative_tab;
	
	protected Item vanilla_item;
	
	
	@Override
	public void build( JsonData data, String fallback_name, IContentBuildContext ctx )
	{
		super.build( data, fallback_name, ctx );
		
		this.vanilla_item = this._setupVanillaItem( ctx );
		
		// Item creative tab may not be loaded yet, so we defer it to post load.
		FMUM.SIDE.runIfClient( () -> ctx.regisPostLoadCallback( this::_checkCreativeTab ) );
	}
	
	@Override
	public void reload( JsonData data, IContentBuildContext ctx )
	{
		super.reload( data, ctx );
		
		this.creative_tab = data.getString( "creative_tab" ).orElse( "none" );
	}
	
	protected abstract Item _setupVanillaItem( IContentBuildContext ctx );
	
	@SideOnly( Side.CLIENT )
	protected void _checkCreativeTab( IPostLoadContext ctx )
	{
		if ( !this.creative_tab.equals( "none" ) )
		{
			final CreativeTabs tab = (
				CreativeTabUtil.lookup( this.creative_tab )
				.orElseGet( ctx::getFallbackCreativeTab )
			);
			this.vanilla_item.setCreativeTab( tab );
		}
	}
	
	@Override
	public String getName() {
		return this.name;
	}
}
