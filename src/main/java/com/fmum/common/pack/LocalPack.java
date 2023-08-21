package com.fmum.common.pack;

import com.fmum.common.FMUM;
import com.fmum.common.load.ContentBuildContext;
import com.fmum.common.load.LoaderNotFoundException;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.io.Reader;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

public abstract class LocalPack implements ContentPackFactory, ContentPack
{
	protected static final String ERROR_LOADING_TYPE = "fmum.error_loading_type";
	
	protected final ModContainer mod_container;
	protected final HashSet< String > ignored_entries = new HashSet<>();
	
	protected LocalPack( ModContainer mod_container )
	{
		this.mod_container = mod_container;
		this.ignored_entries.add( "assets" );
	}
	
	@Override
	public String name() {
		return this.mod_container.getName();
	}
	
	@Override
	public String author() {
		return String.join( ", ", this.mod_container.getMetadata().authorList );
	}
	
	@Override
	public String resourceDomain() {
		return this.mod_container.getModId();
	}
	
	@Override
	public String sourceName() {
		return this.mod_container.getSource().getName();
	}
	
	@Override
	public ContentPack createServerSide( IPrepareContext ctx )
	{
		ctx.regisLoadCallback( ctx_ -> {
			FMUM.MOD.logInfo( "fmum.load_content_pack", this.name() );
			this._loadPackContent( ctx_ );
		} );
		return this;
	}
	
	@Override
	@SideOnly( Side.CLIENT )
	public ContentPack createClientSide( IPrepareContext ctx )
	{
		this.createServerSide( ctx );

		// Load key binds on client side.
		ctx.regisLoadCallback( this::_loadKeyBinds )
		return this;
	}
	
	protected abstract void _loadPackContent( ILoadContext ctx );
	
	@SideOnly( Side.CLIENT )
	protected void _loadKeyBind( ILoadContext ctx )
	{
		
	}
	
	protected Optional< Object > _loadJsonEntry(
		Reader in,
		String fallback_type,
		String file_path,
		ILoadContext ctx
	) {
		// Check if it has specified a type(loader).
		final JsonObject obj = ctx.gson().fromJson( in, JsonObject.class );
		final String loader = Optional.ofNullable( obj.get( "__type__" ) )
			.map( JsonElement::getAsString ).orElse( fallback_type );
		
		final ContentBuildContext build_context = new ContentBuildContext()
		{
			@Override
			public String fallbackName()
			{
				final int start = file_path.lastIndexOf( '/' );
				final int end = file_path.length() - ".json".length();
				return file_path.substring( start, end );
			}
			
			@Override
			public ContentPack contentPack() {
				return LocalPack.this;
			}
			
			@Override
			public Gson gson() {
				return ctx.gson();
			}
			
			@Override
			public void regisPostLoadCallback(
				Consumer< IPostLoadContext > callback
			) { ctx.regisPostLoadCallback( callback ); }
		};
		
		try
		{
			return Optional.of(
				ctx.loadContent( loader, obj, build_context ) );
		}
		catch ( LoaderNotFoundException e )
		{
			final String path = this.sourceName() + "/" + file_path;
			FMUM.MOD.logError( "fmum.type_loader_not_found", path, loader );
		}
		return Optional.empty();
	}
	
	protected static class PackMetadataTemplate
	{
		public String name;
		public String author;
		public Set< String > ignored_entries = Collections.emptySet();
	}
}
