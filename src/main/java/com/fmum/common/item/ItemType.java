package com.fmum.common.item;

import com.fmum.common.pack.IBuildable;
import com.google.gson.annotations.SerializedName;
import net.minecraft.item.Item;

public class ItemType extends Item implements IItemType, IBuildable< ItemType >
{
	protected String name;
	
	@SerializedName( value = "creative_tab", alternate = "item_group" )
	protected String creative_tab = FMUM.DEFAULT_TAB.name();
	
	@Override
	public ItemType build()
	{
		IItemType.REGISTRY.regis( this );
		
		return this;
	}
}
