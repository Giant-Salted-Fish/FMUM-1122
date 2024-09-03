package com.fmum.tab;

import com.fmum.FMUM;
import com.fmum.load.BuildableType;
import com.fmum.load.IContentBuildContext;
import com.fmum.load.IPostLoadContext;
import com.fmum.render.Texture;
import com.google.gson.JsonObject;
import com.google.gson.annotations.Expose;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import java.util.Optional;
import java.util.function.Supplier;

public class JsonCreativeTab extends BuildableType
{
	@Expose
	@SideOnly( Side.CLIENT )
	protected Supplier< Optional< ItemStack > > icon_item;
	
	@Expose
	@SideOnly( Side.CLIENT )
	protected boolean no_scroll_bar;
	
	@Expose
	@SideOnly( Side.CLIENT )
	protected boolean no_title;
	
	@Expose
	@SideOnly( Side.CLIENT )
	protected Texture background_image;
	
	
	@SideOnly( Side.CLIENT )
	protected ItemStack icon_stack;
	
	
	@Override
	public void build( JsonObject data, String fallback_name, IContentBuildContext ctx )
	{
		super.build( data, fallback_name, ctx );
		
		FMUM.SIDE.runIfClient( () -> {
			if ( this.background_image == null )
			{
				final CreativeTabs mc_tab = CreativeTabs.BUILDING_BLOCKS;
				this.background_image = new Texture( mc_tab.getBackgroundImage() );
			}
			
			ctx.regisPostLoadCallback( this::_setupIconItem );
		} );
		
		this._createVanillaTab();
	}
	
	@SideOnly( Side.CLIENT )
	protected void _setupIconItem( IPostLoadContext ctx ) {
		this.icon_stack = this.icon_item.get().orElseGet( ctx::getFallbackTabIconItem );
	}
	
	protected void _createVanillaTab()
	{
		final CreativeTabs tab = new CreativeTabs( this.name ) {
			@Nonnull
			@Override
			@SideOnly( Side.CLIENT )
			public ItemStack createIcon()
			{
				final ItemStack stack = JsonCreativeTab.this.icon_stack;
				JsonCreativeTab.this.icon_stack = null;
				return stack;
			}
			
			@Nonnull
			@Override
			@SideOnly( Side.CLIENT )
			public String getBackgroundImageName() {
				return JsonCreativeTab.this.background_image.getPath();
			}
			
			@Nonnull
			@Override
			@SideOnly( Side.CLIENT )
			public ResourceLocation getBackgroundImage() {
				return JsonCreativeTab.this.background_image;
			}
		};
		
		FMUM.SIDE.runIfClient( () -> {
			if ( this.no_scroll_bar ) {
				tab.setNoScrollbar();
			}
			if ( this.no_title ) {
				tab.setNoTitle();
			}
		} );
	}
}
