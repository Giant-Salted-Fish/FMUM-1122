package com.fmum.common.tab;

import com.fmum.common.Registry;
import com.fmum.common.item.IItemType;
import net.minecraft.creativetab.CreativeTabs;

public interface ICreativeTab
{
	Registry< ICreativeTab > REGISTRY = new Registry<>( ICreativeTab::name );
	
	String name();
	
	CreativeTabs vanillaCreativeTab();
	
	default void appendItem( IItemType item ) {
		item.vanillaItem().setCreativeTab( this.vanillaCreativeTab() );
	}
}
