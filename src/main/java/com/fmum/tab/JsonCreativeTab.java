package com.fmum.tab;

import com.fmum.FMUM;
import com.fmum.item.JsonItemStack;
import com.fmum.load.BuildableType;
import com.fmum.load.IContentBuildContext;
import com.fmum.load.IContentLoader;
import com.fmum.load.IPostLoadContext;
import com.fmum.load.JsonData;
import com.fmum.render.Texture;
import com.google.gson.JsonObject;
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
	public static final IContentLoader< JsonCreativeTab >
		LOADER = IContentLoader.of( JsonCreativeTab::new );
	
	
	@SideOnly( Side.CLIENT )
	protected Supplier< Optional< ItemStack > > icon_item;
	
	@SideOnly( Side.CLIENT )
	protected boolean no_scroll_bar;
	
	@SideOnly( Side.CLIENT )
	protected boolean no_title;
	
	@SideOnly( Side.CLIENT )
	protected Texture background_image;
	
	@SideOnly( Side.CLIENT )
	protected ItemStack icon_stack;
	
	
	@Override
	public void build( JsonObject data, String fallback_name, IContentBuildContext ctx )
	{
		super.build( data, fallback_name, ctx );
		
		FMUM.SIDE.runIfClient( () -> ctx.regisPostLoadCallback( this::_setupIconItem ) );
		this._createVanillaTab();
	}
	
	@Override
	public void reload( JsonObject json, IContentBuildContext ctx )
	{
		super.reload( json, ctx );
		
		FMUM.SIDE.runIfClient( () -> {
			final JsonData data = new JsonData( json, ctx.getGson() );
			this.icon_item = (
				data.get( "icon_item", JsonItemStack.class )
				.map( jis -> ( Supplier< Optional< ItemStack > > ) jis::create )
				.orElse( Optional::empty )
			);
			this.no_scroll_bar = data.getBool( "no_scroll_bar" ).orElse( false );
			this.no_title = data.getBool( "no_title" ).orElse( false );
			this.background_image = data.get( "background_image", Texture.class ).orElseGet( () -> {
				final CreativeTabs mc_tab = CreativeTabs.BUILDING_BLOCKS;
				return new Texture( mc_tab.getBackgroundImage() );
			} );
		} );
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
