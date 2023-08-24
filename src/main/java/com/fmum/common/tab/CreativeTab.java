package com.fmum.common.tab;

import com.fmum.common.Registry;
import com.fmum.common.item.ItemType;
import net.minecraft.creativetab.CreativeTabs;

public interface CreativeTab
{
	Registry< CreativeTab > REGISTRY = new Registry<>( CreativeTab::name );
	
	String name();
	
	CreativeTabs vanillaCreativeTab();
	
	default void regisItem( ItemType item ) {
		item.vanillaItem().setCreativeTab( this.vanillaCreativeTab() );
	}
}
