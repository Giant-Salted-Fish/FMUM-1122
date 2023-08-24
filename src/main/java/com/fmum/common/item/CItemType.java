package com.fmum.common.item;

import com.fmum.common.load.ContentBuildContext;
import com.fmum.common.pack.ContentPackFactory.PostLoadContext;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Optional;

public abstract class CItemType extends net.minecraft.item.Item implements ItemType
{
	protected String name;
	
//	@SerializedName( value = "creative_tab", alternate = "item_group" )
//	protected String creative_tab = FMUM.DEFAULT_CREATIVE_TAB.name();
	
	protected transient net.minecraft.item.Item vanilla_item;
	
	public CItemType buildServerSide( ContentBuildContext ctx )
	{
		ItemType.REGISTRY.regis( this );
		
		this.name = Optional.ofNullable( this.name )
			.orElseGet( ctx::fallbackName );
		this.vanilla_item = this._createVanillaItem();
		
		// Item creative tab may not be loaded yet, so we defer it to post load.
		ctx.regisPostLoadCallback( this::_setupCreativeTab );
		return this;
	}
	
	@SideOnly( Side.CLIENT )
	public CItemType buildClientSide( ContentBuildContext ctx )
	{
		this.buildServerSide( ctx );
		
		return this;
	}
	
	@Override
	public String name() {
		return this.name;
	}
	
	@Override
	public net.minecraft.item.Item vanillaItem()
	{
		return null;
	}
	
	@Override
	public Item getItem( ItemStack stack )
	{
		return null;
	}
	
	protected abstract net.minecraft.item.Item _createVanillaItem();
	
	protected abstract void _setupCreativeTab( PostLoadContext ctx );
}
