package com.fmum.common.tab;

import com.fmum.common.item.IItemType;
import com.fmum.common.meta.IMeta;
import com.fmum.common.meta.Registry;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;

public interface ICreativeTab extends IMeta
{
	Registry< ICreativeTab > REGISTRY = new Registry<>();
	
	CreativeTabs creativeTab();
	
	/**
	 * Called when an item requires to settle in this tab. Can be used to implement classical Flan's
	 * Mod creative tab which its icon changes among the items in it through time. In default just
	 * call {@link Item#setCreativeTab(CreativeTabs)} for given item.
	 * 
	 * @param item Item that settled in.
	 */
	default void itemSettledIn( IItemType item ) {
		item.item().setCreativeTab( this.creativeTab() );
	}
}
