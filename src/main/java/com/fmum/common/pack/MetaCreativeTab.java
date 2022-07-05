package com.fmum.common.pack;

import java.util.HashMap;
import java.util.Map;

import com.fmum.common.FMUM;
import com.fmum.common.meta.MetaBase;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;

/**
 * Super class of the creative tabs in {@link FMUM} which can be loaded from the content packs.
 * Usually every content pack will have its own creative tab.
 * 
 * @see TypeCreativeTab
 * @author Giant_Salted_Fish
 */
public interface MetaCreativeTab extends MetaBase
{
	public static final HashMap< String, MetaCreativeTab > regis = new HashMap<>();
	
	@Override
	public default void regisPostInitHandler( Map< String, Runnable > tasks )
	{
		MetaBase.super.regisPostInitHandler( tasks );
		
		tasks.put( "REGIS_TAB", () -> this.regisTo( this, regis ) );
	}
	
	@Override
	public default void regisPostLoadHandler( Map< String, Runnable > tasks ) {
		MetaBase.super.regisPostLoadHandler( tasks );
	}
	
	public CreativeTabs creativeTab();
	
	/**
	 * Called when an item requires to settle in this tab. Can be used to implement classical Flan's
	 * Mod creative tab which its icon changes between the items in it over the time.
	 * 
	 * @param item Item wants to settle in
	 */
	public default void itemSettleIn( Item item ) { }
	
	public static MetaCreativeTab get( String name ) { return regis.get( name ); }
}
