package com.fmum.common.item;

import java.util.Set;

import com.fmum.common.FMUM;
import com.fmum.common.meta.TypeTextured;
import com.fmum.common.pack.MetaCreativeTab;
import com.fmum.common.util.LocalAttrParser;

import net.minecraft.item.Item;

public abstract class TypeItem extends TypeTextured implements MetaItem
{
	public static final LocalAttrParser< TypeItem >
		parser = new LocalAttrParser<>( TypeTextured.parser );
	static
	{
		parser.addKeyword( "CreativeTab", ( s, t ) -> t.creativeTab = s[ 1 ] );
	}
	
	/**
	 * Corresponding {@link Item} to this type. Usually set on item registration.
	 */
	public Item item = null;
	
	/**
	 * Creative tab where this item will be displayed in
	 */
	public String creativeTab = FMUM.tab.getTabLabel();
	
	public TypeItem( String name ) { super( name ); }
	
	@Override
	public void regisPostInitHandler( Set< Runnable > tasks )
	{
		super.regisPostInitHandler( tasks );
		MetaItem.super.regisPostInitHandler( tasks );
		
		tasks.add( () -> this.setupItem() );
	}
	
	@Override
	public void regisPostLoadHandler( Set< Runnable > tasks )
	{
		super.regisPostLoadHandler( tasks );
		MetaItem.super.regisPostLoadHandler( tasks );
	}
	
	@Override
	public Item item() { return this.item; }
	
	protected abstract void setupItem();
	
	/**
	 * Call {@link #withItem(Item, int, int)} with parameter list {@code (item, 1, 0)}
	 */
	protected final void withItem( Item item ) { this.withItem( item, 1, 0 ); }
	
	/**
	 * Helper method that initializes the given item and binds it to this type
	 * 
	 * @param item Item to bind
	 * @param maxStackSize Max stack size of this item
	 * @param maxDamage Max item of this item
	 * @return Parameter {@code item}
	 */
	protected final void withItem( Item item, int maxStackSize, int maxDamage )
	{
		this.item = item;
		item.setRegistryName( this.name );
		item.setTranslationKey( this.name );
		item.setMaxStackSize( maxStackSize );
		item.setMaxDamage( maxDamage );
		
		// Try set required creative tab
		MetaCreativeTab tab = MetaCreativeTab.get( this.creativeTab );
		if( tab == null )
		{
			this.log().error(
				this.format( "fmum.failtofetchcreativetab", this.creativeTab, this.toString() )
			);
			tab = FMUM.tab;
		}
		item.setCreativeTab( tab.creativeTab() );
		tab.itemSettleIn( item );
	}
}
