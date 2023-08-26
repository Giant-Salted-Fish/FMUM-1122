package com.fmum.common.item;

import com.fmum.common.load.IContentBuildContext;
import com.fmum.common.pack.IContentPackFactory.IPostLoadContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Optional;

public abstract class ItemType extends Item implements IItemType
{
	protected String name;
	
//	@SerializedName( value = "creative_tab", alternate = "item_group" )
//	protected String creative_tab = FMUM.DEFAULT_CREATIVE_TAB.name();
	
	protected transient Item vanilla_item;
	
	public ItemType buildServerSide( IContentBuildContext ctx )
	{
		IItemType.REGISTRY.regis( this );
		
		this.name = Optional.ofNullable( this.name )
			.orElseGet( ctx::fallbackName );
		this.vanilla_item = this._createVanillaItem();
		
		// Item creative tab may not be loaded yet, so we defer it to post load.
		ctx.regisPostLoadCallback( this::_setupCreativeTab );
		return this;
	}
	
	@SideOnly( Side.CLIENT )
	public ItemType buildClientSide( IContentBuildContext ctx )
	{
		this.buildServerSide( ctx );
		
		return this;
	}
	
	@Override
	public String name() {
		return this.name;
	}
	
	@Override
	public Item vanillaItem()
	{
		return null;
	}
	
	@Override
	public IItem getItem( ItemStack stack )
	{
		return null;
	}
	
	protected abstract Item _createVanillaItem();
	
	protected abstract void _setupCreativeTab( IPostLoadContext ctx );
}
