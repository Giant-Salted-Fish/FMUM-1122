package com.fmum.common.pack;

import java.util.Map;

import com.fmum.client.ResourceHandler;
import com.fmum.common.FMUM;
import com.fmum.common.item.MetaItem;
import com.fmum.common.meta.EnumMeta;

import net.minecraft.client.resources.I18n;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * A default implementation of the creative tabs in {@link FMUM} that allows you to specify the tab
 * icon item and background image
 * 
 * @see MetaCreativeTab
 * @author Giant_Salted_Fish
 */
public class TypeCreativeTab extends CreativeTabs implements MetaCreativeTab
{
	public static final TypeParser< TypeCreativeTab >
		parser = new TypeParser< TypeCreativeTab >( TypeCreativeTab.class, null );
	static
	{
		parser.addKeyword(
			"IconItem",
			( s, t ) -> {
				t.iconItem = s[ 1 ];
				if( s.length < 3 ) return;
				
				t.iconItemDam = Short.parseShort( s[ 2 ] );
			}
		);
		parser.addKeyword( "IconItemDam", ( s, t ) -> t.iconItemDam = Short.parseShort( s[ 1 ] ) );
		parser.addKeyword(
			"BackgroundImage",
			( s, t ) -> {
				t.setBackgroundImageName( s[ 1 ] );
				
				// Set a dirty mark
				t.backgroundImage = null;
			}
		);
		parser.addKeyword( "NoTitle", ( s, t ) -> t.setNoTitle() );
		parser.addKeyword( "NoScrollBar", ( s, t ) -> t.setNoScrollbar() );
	}
	
	protected static final ItemStack DEF_ICON_STACK = new ItemStack( Blocks.WOOL, 1, 10 );
	
	// TODO: change this to a valid item name
	// TODO: maybe set to side only?
	public String iconItem = "undefined";
	
	public short iconItemDam = 0;
	
	/**
	 * Background image used for this creative tab
	 */
	public ResourceLocation backgroundImage;
	
	/**
	 * {@link CreativeTabs#getTabLabel()} is client side only hence this is needed on server side to
	 * provide name
	 */
	@SideOnly( Side.SERVER )
	protected String name;
	
	public TypeCreativeTab( String label )
	{
		super( label );
		
		if( FMUM.MOD.isClient() )
			this.backgroundImage = super.getBackgroundImage();
		else this.name = label;
	}
	
	@Override
	public void regisPostInitHandler( Map< String, Runnable > tasks )
	{
		MetaCreativeTab.super.regisPostInitHandler( tasks );
		
		// Setup background image
		tasks.put(
			"SETUP_BG_IMG",
			() -> {
				if( this.backgroundImage == null )
					this.backgroundImage = ResourceHandler.getTexture( this.getBackgroundImageName() );
			}
		);
	}
	
	@Override
	public void regisPostLoadHandler( Map< String, Runnable > tasks ) {
		MetaCreativeTab.super.regisPostLoadHandler( tasks );
	}
	
	@Override
	public CreativeTabs creativeTab() { return this; }
	
	// FIXME: getTabLabel is side only!
	@Override
	public String name() { return FMUM.MOD.isClient() ? this.getTabLabel() : this.name; }
	
	@Override
	@SideOnly( Side.CLIENT )
	public ItemStack getTabIconItem()
	{
		// Check if it requires an item defined in FMUM
		MetaItem type = MetaItem.get( this.iconItem );
		Item item = type != null ? type.item() : Item.getByNameOrId( this.iconItem );
		if( item != null ) return new ItemStack( item, 1, this.iconItemDam );
		
		this.log().error(
			I18n.format(
				"fmum.failtofindiconitemfortab",
				this.iconItem,
				this.getTabLabel()
			)
		);
		
		// Return a purple wool to indicate a fault
		return DEF_ICON_STACK;
	}
	
	@Override
	@SideOnly( Side.CLIENT )
	public ResourceLocation getBackgroundImage() { return this.backgroundImage; }
	
	@Override
	@SideOnly( Side.CLIENT )
	public ResourceLocation texture() { return this.backgroundImage; }
	
	@Override
	public EnumMeta enumMeta() { return EnumMeta.TAB; }
	
	@Override
	public String toString() { return this.identifier(); }
}
