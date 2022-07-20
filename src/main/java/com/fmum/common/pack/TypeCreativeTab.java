package com.fmum.common.pack;

import java.util.Map;

import com.fmum.client.ResourceManager;
import com.fmum.common.FMUM;
import com.fmum.common.item.MetaItem;

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
		parser = new TypeParser<>( TypeCreativeTab.class, null );
	static
	{
		parser.addKeyword( "NoTitle", ( s, t ) -> t.setNoTitle() );
		parser.addKeyword( "NoScrollBar", ( s, t ) -> t.setNoScrollbar() );
	}
	
	public static final TypeParser< TypeCreativeTab >
		parserClient = new TypeParser<>( TypeCreativeTab.class, parser );
	static
	{
		parserClient.addKeyword(
			"IconItem",
			( s, t ) -> {
				t.iconItem = s[ 1 ];
				if( s.length < 3 ) return;
				
				t.iconItemDam = Short.parseShort( s[ 2 ] );
			}
		);
		parserClient.addKeyword(
			"IconItemDam",
			( s, t ) -> t.iconItemDam = Short.parseShort( s[ 1 ] )
		);
		parserClient.addKeyword(
			"BackgroundImage",
			( s, t ) -> {
				t.setBackgroundImageName( s[ 1 ] );
				
				// Set a dirty mark
				t.backgroundImage = null;
			}
		);
	}
	
	/**
	 * Icon item that will appear if the require item can not be found. In default is the purple
	 * wool block.
	 */
	@SideOnly( Side.CLIENT )
	protected static ItemStack defIcon;
	
	@SideOnly( Side.CLIENT )
	protected static String defIconItem;
	
	@SideOnly( Side.CLIENT )
	protected static short defIconDam;
	
	static { if( FMUM.MOD.isClient() )
	{
		defIcon = new ItemStack( Blocks.WOOL, 1, 10 );
		defIconItem = defIcon.getItem().getRegistryName().toString();
		defIconDam = ( short ) defIcon.getItemDamage();
	} }
	
	/**
	 * {@link CreativeTabs#getTabLabel()} is client side only hence this is needed on server side to
	 * provide name
	 */
	@SideOnly( Side.SERVER )
	protected String name;
	
	@SideOnly( Side.CLIENT )
	public String iconItem;
	
	@SideOnly( Side.CLIENT )
	public short iconItemDam;
	
	/**
	 * Background image used for this creative tab
	 */
	@SideOnly( Side.CLIENT )
	public ResourceLocation backgroundImage;
	
	public TypeCreativeTab( String label )
	{
		super( label );
		
		if( FMUM.MOD.isClient() )
		{
			this.iconItem = defIconItem;
			this.iconItemDam = defIconDam;
			this.backgroundImage = super.getBackgroundImage();
		}
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
				if( !FMUM.MOD.isClient() || this.backgroundImage != null ) return;
				
				this.backgroundImage = ResourceManager.getTexture( this.getBackgroundImageName() );
			}
		);
	}
	
	@Override
	public void regisPostLoadHandler( Map< String, Runnable > tasks ) {
		MetaCreativeTab.super.regisPostLoadHandler( tasks );
	}
	
	@Override
	public CreativeTabs creativeTab() { return this; }
	
	@Override
	public String name() { return FMUM.MOD.isClient() ? this.getTabLabel() : this.name; }
	
	@Override
	@SideOnly( Side.CLIENT )
	public ItemStack getTabIconItem()
	{
		// Check if required item is defined in FMUM
		MetaItem type = MetaItem.get( this.iconItem );
		Item item = type != null ? type.item() : Item.getByNameOrId( this.iconItem );
		if( item != null ) return new ItemStack( item, 1, this.iconItemDam );
		
		this.log().error( I18n.format(
			"fmum.cannotfindtabiconitem",
			this.getTabLabel(),
			this.iconItem
		) );
		return defIcon;
	}
	
	@Override
	@SideOnly( Side.CLIENT )
	public ResourceLocation getBackgroundImage() { return this.backgroundImage; }
	
	@Override
	@SideOnly( Side.CLIENT )
	public ResourceLocation texture() { return this.backgroundImage; }
	
	@Override
	public String toString() { return this.identifier(); }
}
