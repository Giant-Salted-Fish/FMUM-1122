package com.fmum.common.item;

import java.util.Map;

import com.fmum.common.FMUM;
import com.fmum.common.meta.TypeTextured;
import com.fmum.common.pack.MetaCreativeTab;
import com.fmum.common.pack.TypeParser;

import net.minecraft.item.Item;

public abstract class TypeItem extends TypeTextured implements MetaItem
{
	public static final TypeParser< TypeItem >
		parser = new TypeParser<>( TypeTextured.parser );
	static
	{
		parser.addKeyword( "CreativeTab", ( s, t ) -> t.creativeTab = s[ 1 ] );
	}
	
	/**
	 * Corresponding {@link Item} to this type. Usually set on item registration.
	 * 
	 * @see #createItem()
	 */
	public Item item = null;
	
	/**
	 * Creative tab where this item will be displayed in
	 */
	public String creativeTab = FMUM.tab.getTabLabel();
	
	public TypeItem( String name ) { super( name ); }
	
	@Override
	public void regisPostInitHandler( Map< String, Runnable > tasks )
	{
		super.regisPostInitHandler( tasks );
		MetaItem.super.regisPostInitHandler( tasks );
		
		tasks.put( "SETUP_ITEM", () -> this.createItem() );
	}
	
	@Override
	public void regisPostLoadHandler( Map< String, Runnable > tasks )
	{
		super.regisPostLoadHandler( tasks );
		MetaItem.super.regisPostLoadHandler( tasks );
		
		// Locate creative tab and set it for item here as the customized creative may not be \
		// loaded right after the parse of the type item
		tasks.put(
			"SETTLE_TAB",
			() -> {
				// Try set required creative tab
				MetaCreativeTab tab = MetaCreativeTab.get( this.creativeTab );
				if( tab == null )
				{
					this.log().error( this.format(
						"fmum.failtofindcreativetab",
						this.creativeTab,
						this.toString()
					) );
					tab = FMUM.tab;
				}
				this.item.setCreativeTab( tab.creativeTab() );
				tab.itemSettleIn( this.item );
			}
		);
	}
	
	@Override
	public Item item() { return this.item; }
	
	protected abstract void createItem();
	
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
		item.setUnlocalizedName( this.name );
		item.setMaxStackSize( maxStackSize );
		item.setMaxDamage( maxDamage );
	}
}
