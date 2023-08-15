package com.fmum.common.tab;

import com.fmum.client.ModConfigClient;
import com.fmum.common.item.IItemType;
import com.fmum.common.pack.IContentBuildContext;
import com.google.gson.annotations.SerializedName;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import java.util.Optional;

public class CreativeTab implements ICreativeTab
{
	protected transient CreativeTabs vanilla_creative_tab;
	
	protected String name;
	
	@SideOnly( Side.CLIENT )
	@SerializedName( value = "icon_item" )
	protected String icon_item_name;
	
	@SideOnly( Side.CLIENT )
	protected short icon_item_meta;
	
	@SideOnly( Side.CLIENT )
	protected boolean no_scroll_bar;
	
	@SideOnly( Side.CLIENT )
	protected boolean no_title;
	
	@SideOnly( Side.CLIENT )
	protected transient ItemStack icon_item;
	
	@SideOnly( Side.CLIENT )
	protected ResourceLocation background_image;
	
	public CreativeTab buildServerSide( IContentBuildContext ctx )
	{
		ICreativeTab.REGISTRY.regis( this );
		
		this.name = Optional.ofNullable( this.name ).orElseGet( ctx::fallbackName );
		return this;
	}
	
	@SideOnly( Side.CLIENT )
	public CreativeTab buildClientSide( IContentBuildContext ctx )
	{
		this.buildServerSide( ctx );
		
		this.icon_item_name = Optional.ofNullable( this.icon_item_name )
	 		.orElse( ModConfigClient.default_creative_tab_icon_item );
		this.background_image = Optional.ofNullable( this.background_image )
			.orElseGet( CreativeTabs.BUILDING_BLOCKS::getBackgroundImage );
		
		this.vanilla_creative_tab = this._createVanillaTab();
		if ( this.no_scroll_bar ) {
			this.vanilla_creative_tab.setNoScrollbar(); }
		if ( this.no_title ) {
			this.vanilla_creative_tab.setNoTitle(); }
		
		ctx.regisPostLoadCallback( ctx_ -> {
			final Optional< Item > icon_item = IItemType.findItem( this.icon_item_name );
			final int meta = CreativeTab.this.icon_item_meta;
			this.icon_item = icon_item
				.map( item -> new ItemStack( item, 1, meta ) )
				.orElseGet( ctx_::defaultTabIconItem );
		} );
		return this;
	}
	
	protected CreativeTabs _createVanillaTab() {
		return new VanillaCreativeTab();
	}
	
	@Override
	public String name() {
		return this.name;
	}
	
	@Override
	public CreativeTabs vanillaCreativeTab() {
		return this.vanilla_creative_tab;
	}
	
	protected class VanillaCreativeTab extends CreativeTabs
	{
		protected VanillaCreativeTab() {
			super( CreativeTab.this.name );
		}
		
		@Nonnull
		@Override
		@SideOnly( Side.CLIENT )
		public ItemStack createIcon() {
			return CreativeTab.this.icon_item;
		}
		
		@Nonnull
		@Override
		@SideOnly( Side.CLIENT )
		public ResourceLocation getBackgroundImage() {
			return CreativeTab.this.background_image;
		}
	}
}
