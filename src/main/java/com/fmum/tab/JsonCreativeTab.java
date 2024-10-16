package com.fmum.tab;

import com.fmum.FMUM;
import com.fmum.item.IItemType;
import com.fmum.load.BuildableType;
import com.fmum.load.IContentBuildContext;
import com.fmum.load.IContentLoader;
import com.fmum.load.IPostLoadContext;
import com.fmum.load.JsonData;
import com.fmum.render.Texture;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

public class JsonCreativeTab extends BuildableType
{
	public static final IContentLoader< JsonCreativeTab >
		LOADER = IContentLoader.of( JsonCreativeTab::new );
	
	
	@SideOnly( Side.CLIENT )
	protected String icon_item;
	
	@SideOnly( Side.CLIENT )
	protected short icon_meta;
	
	@SideOnly( Side.CLIENT )
	protected boolean no_scroll_bar;
	
	@SideOnly( Side.CLIENT )
	protected boolean no_title;
	
	@SideOnly( Side.CLIENT )
	protected Texture background_image;
	
	@SideOnly( Side.CLIENT )
	protected ItemStack icon_stack;
	
	
	@Override
	public void build( JsonData data, String fallback_name, IContentBuildContext ctx )
	{
		super.build( data, fallback_name, ctx );
		
		FMUM.SIDE.runIfClient( () -> ctx.regisPostLoadCallback( this::_setupIconItem ) );
		this._createVanillaTab();
	}
	
	@Override
	public void reload( JsonData data, IContentBuildContext ctx )
	{
		super.reload( data, ctx );
		
		FMUM.SIDE.runIfClient( () -> {
			this.icon_item = data.getString( "icon_item" ).orElseGet( () -> {
				final ResourceLocation regis_name = Items.FISH.getRegistryName();
				return Objects.requireNonNull( regis_name ).toString();
			} );
			this.icon_meta = data.get( "icon_meta", short.class ).orElse( ( short ) 0 );
			this.no_scroll_bar = data.getBool( "no_scroll_bar" ).orElse( false );
			this.no_title = data.getBool( "no_title" ).orElse( false );
			this.background_image = data.get( "background_image", Texture.class ).orElseGet( () -> {
				final CreativeTabs mc_tab = CreativeTabs.BUILDING_BLOCKS;
				return new Texture( mc_tab.getBackgroundImage() );
			} );
		} );
	}
	
	@SideOnly( Side.CLIENT )
	protected void _setupIconItem( IPostLoadContext ctx )
	{
		final Optional< Function< Short, ItemStack > > factory = IItemType.lookupItemFactory( this.icon_item );
		if ( factory.isPresent() ) {
			this.icon_stack = factory.get().apply( this.icon_meta );
		}
		else
		{
			FMUM.LOGGER.error( "Can not find icon item <{}> required by <{}>", this.icon_item, this.name );
			this.icon_stack = ctx.getFallbackTabIconItem();
		}
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
