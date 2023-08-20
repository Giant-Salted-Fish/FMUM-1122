package com.fmum.common.tab;

import com.fmum.client.ModConfigClient;
import com.fmum.common.item.IItemType;
import com.fmum.common.load.BuildableType;
import com.fmum.common.load.ContentBuildContext;
import com.google.gson.annotations.SerializedName;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import java.util.Optional;

public class JsonCreativeTab extends BuildableType implements CreativeTab
{
	protected transient CreativeTabs vanilla_creative_tab;
	
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
	
	@Override
	public void buildServerSide( ContentBuildContext ctx )
	{
		super.buildServerSide( ctx );
		
		CreativeTab.REGISTRY.regis( this );
	}
	
	@Override
	@SideOnly( Side.CLIENT )
	public void buildClientSide( ContentBuildContext ctx )
	{
		super.buildClientSide( ctx );
		
		this.icon_item_name = Optional.ofNullable( this.icon_item_name )
	 		.orElse( ModConfigClient.default_creative_tab_icon_item );
		this.background_image = Optional.ofNullable( this.background_image )
			.orElseGet( CreativeTabs.BUILDING_BLOCKS::getBackgroundImage );
		
		this.vanilla_creative_tab = this._createVanillaTab();
		if ( this.no_scroll_bar ) {
			this.vanilla_creative_tab.setNoScrollbar(); }
		if ( this.no_title ) {
			this.vanilla_creative_tab.setNoTitle(); }
		
		ctx.regisPostLoadCallback(
			ctx_ -> this.icon_item = IItemType.findItem( this.icon_item_name )
				.map( item -> new ItemStack( item, 1, this.icon_item_meta ) )
				.orElseGet( ctx_::defaultTabIconItem )
		);
	}
	
	@Override
	public CreativeTabs vanillaCreativeTab() {
		return this.vanilla_creative_tab;
	}
	
	protected CreativeTabs _createVanillaTab() {
		return new VanillaCreativeTab();
	}
	
	@Override
	protected String _typeHint() {
		return "CREATIVE_TAB";
	}
	
	protected class VanillaCreativeTab extends CreativeTabs
	{
		protected VanillaCreativeTab() {
			super( JsonCreativeTab.this.name );
		}
		
		@Nonnull
		@Override
		@SideOnly( Side.CLIENT )
		public ItemStack createIcon() {
			return JsonCreativeTab.this.icon_item;
		}
		
		@Nonnull
		@Override
		@SideOnly( Side.CLIENT )
		public ResourceLocation getBackgroundImage() {
			return JsonCreativeTab.this.background_image;
		}
	}
}
