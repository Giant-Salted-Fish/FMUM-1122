package com.fmum.common.item;

import com.fmum.common.FMUM;
import com.fmum.common.pack.ILoadablePack.IBuildContext;
import com.fmum.common.pack.ILoadablePack.IBuildableContent;
import com.google.gson.annotations.SerializedName;
import net.minecraft.item.Item;

import java.util.Optional;

public class ItemType extends Item implements IItemType, IBuildableContent< ItemType >
{
	protected String name;
	
	@SerializedName( value = "creative_tab", alternate = "item_group" )
	protected String creative_tab = FMUM.DEFAULT_CREATIVE_TAB.name();
	
	@Override
	public ItemType buildServerSide( IBuildContext ctx )
	{
		IItemType.REGISTRY.regis( this );
		this.name = Optional.ofNullable( this.name ).orElseGet( ctx::fallbackName );
		return this;
	}
	
	@Override
	public ItemType buildClientSide( IBuildContext ctx )
	{
		this.buildServerSide( ctx );
		
		return this;
	}
}
