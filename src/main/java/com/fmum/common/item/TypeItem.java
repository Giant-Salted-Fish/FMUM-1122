package com.fmum.common.item;

import java.util.Map;

import com.fmum.client.item.RenderableItem;
import com.fmum.common.FMUM;
import com.fmum.common.meta.TypeRenderable;
import com.fmum.common.pack.MetaCreativeTab;
import com.fmum.common.pack.TypeParser;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MouseHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public abstract class TypeItem< T extends RenderableItem > extends TypeRenderable< T >
	implements MetaItem
{
	public static final TypeParser< TypeItem< ? > >
		parser = new TypeParser<>( TypeRenderable.parser );
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
	public String creativeTab = FMUM.tab.name();
	
	public TypeItem( String name ) { super( name ); }
	
	@Override
	public void regisPostInitHandler( Map< String, Runnable > tasks )
	{
		super.regisPostInitHandler( tasks );
		MetaItem.super.regisPostInitHandler( tasks );
		
		// Usually delaying the item creation to post load phase will not cause a problem. Here \
		// setting it at post initialization is to guarantee the completeness of the meta at post \
		// load phase. In this case any meta that wants the item of another meta will not get null \
		// as return.
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
	
	@Override
	public void onRenderTick( ItemStack stack, MouseHelper mouse ) {
		this.model.onRenderTick( stack, MetaHostItem.getMeta( stack ), mouse );
	}
	
	@Override
	@SideOnly( Side.CLIENT )
	public void onRenderInHand( ItemStack stack ) {
		this.model.renderInHand( stack, this );
	}
	
	protected abstract void createItem();
	
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
