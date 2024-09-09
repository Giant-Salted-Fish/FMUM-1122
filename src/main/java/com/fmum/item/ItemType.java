package com.fmum.item;

import com.fmum.FMUM;
import com.fmum.load.BuildableType;
import com.fmum.load.IContentBuildContext;
import com.fmum.load.IPostLoadContext;
import com.fmum.tab.CreativeTabUtil;
import com.google.gson.JsonObject;
import com.google.gson.annotations.Expose;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Optional;

public abstract class ItemType extends BuildableType implements IItemType
{
	@Expose
	protected String creative_tab;
	
	
	protected Item vanilla_item;
	
	
	@Override
	public void build( JsonObject data, String fallback_name, IContentBuildContext ctx )
	{
		super.build( data, fallback_name, ctx );
		
		this.vanilla_item = this._setupVanillaItem( ctx );
		
		// Item creative tab may not be loaded yet, so we defer it to post load.
		FMUM.SIDE.runIfClient( () -> ctx.regisPostLoadCallback( this::_checkCreativeTab ) );
	}
	
	protected abstract Item _setupVanillaItem( IContentBuildContext ctx );
	
	@SideOnly( Side.CLIENT )
	protected void _checkCreativeTab( IPostLoadContext ctx )
	{
		final CreativeTabs tab = (
			Optional.ofNullable( this.creative_tab )
			.flatMap( CreativeTabUtil::lookup )
			.orElseGet( ctx::getFallbackCreativeTab )
		);
		this.vanilla_item.setCreativeTab( tab );
	}
	
	@Override
	public String getName() {
		return this.name;
	}
}
