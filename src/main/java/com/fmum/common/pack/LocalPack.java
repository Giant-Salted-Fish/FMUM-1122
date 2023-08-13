package com.fmum.common.pack;

import com.fmum.common.FMUM;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.io.Reader;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

public abstract class LocalPack implements ILoadablePack, IContentPack
{
	protected static final String ERROR_LOADING_TYPE = "fmum.error_loading_type";
	
	protected final ModContainer mod_container;
	protected final HashSet< String > ignored_entries = new HashSet<>();
	
	private final LinkedList< Runnable > post_load_callbacks = new LinkedList<>();
	
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
	public String sourceName() {
		return this.mod_container.getSource().getName();
	}
	
	@Override
	public Function< ILoadContext, Supplier< IContentPack > > prepareLoadServerSide( IPrepareContext ctx )
	{
		return ctx_ -> {
			FMUM.MOD.logInfo( "fmum.load_content_pack", this.sourceName() );
			this._loadPackContent( ctx_ );
			
			return () -> {
				this.post_load_callbacks.forEach( Runnable::run );
				this.post_load_callbacks.clear();
				return this;
			};
		};
	}
	
	@Override
	@SideOnly( Side.CLIENT )
	public Function< ILoadContext, Supplier< IContentPack > > prepareLoadClientSide(
		IPrepareContext ctx
	) { return this.prepareLoadServerSide( ctx ); }
	
	protected abstract void _loadPackContent( ILoadContext ctx );
	
	protected Optional< Object > _loadJsonEntry(
		Reader in,
		String fallback_type,
		String file_path,
		ILoadContext ctx
	) {
		final JsonObject obj = ctx.gson().fromJson( in, JsonObject.class );
		
		// Check if it has specified a type.
		final JsonElement type = obj.get( "__type__" );
		final String loader_entry = type != null ? type.getAsString() : fallback_type;
		
		final IBuildContext build_context = new IBuildContext()
		{
			@Override
			public String fallbackName()
			{
				final int start = file_path.lastIndexOf( '/' );
				final int end = file_path.length() - ".json".length();
				return file_path.substring( start, end );
			}
			
			@Override
			public IContentPack contentPack() {
				return LocalPack.this;
			}
			
			@Override
			public Gson gson() {
				return ctx.gson();
			}
			
			@Override
			public void regisPostLoadCallback( Runnable callback ) {
				LocalPack.this.post_load_callbacks.add( callback );
			}
		};
		
		try { return Optional.of( ctx.loadContent( loader_entry, obj, build_context ) ); }
		catch ( LoaderNotFoundException e )
		{
			final String path = this.sourceName() + "/" + file_path;
			FMUM.MOD.logError( "fmum.type_loader_not_found", path, loader_entry );
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
