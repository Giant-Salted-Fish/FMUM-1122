package com.fmum.common.pack;

import com.fmum.common.FMUM;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.io.File;
import java.io.Reader;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Optional;
import java.util.Set;

public abstract class LocalPack implements ILoadablePack, IContentPack
{
	protected static final String ERROR_LOADING_INFO = "fmum.error_loading_pack_info";
	protected static final String ERROR_LOADING_TYPE = "fmum.error_loading_type";
	
	protected static final String META_FILE_PATH = "pack.json";
	
	protected final File source;
	protected final HashSet< String > ignored_entries = new HashSet<>();
	
	protected String name;
	protected String author = "fmum.author_missing";
	
	private final LinkedList< Runnable > post_load_callbacks = new LinkedList<>();
	
	protected LocalPack( File source )
	{
		this.source = source;
		this.name = source.getName();
		this.ignored_entries.add( "assets" );
	}
	
	@Override
	public String name() {
		return this.name;
	}
	
	@Override
	public String author() {
		return this.author;
	}
	
	@Override
	public String sourceName() {
		return this.source.getName();
	}
	
	@Override
	public void prepareLoadServerSide( IPrepareContext ctx )
	{
		ctx.regisResourceDomain( this.source );
		ctx.regisPackLoader( ctx_ -> {
			FMUM.logInfo( "fmum.load_content_pack", this.sourceName() );
			this._loadContent( ctx_ );
			
			return () -> {
				this.post_load_callbacks.forEach( Runnable::run );
				this.post_load_callbacks.clear();
				return this;
			};
		} );
	}
	
	@Override
	@SideOnly( Side.CLIENT )
	public void prepareLoadClientSide( IPrepareContext ctx ) {
		this.prepareLoadServerSide( ctx );
	}
	
	protected abstract void _loadContent( ILoadContext ctx );
	
	protected void _setupMetaDataWith( PackMetadataTemplate data )
	{
		this.name = Optional.ofNullable( data.name ).orElse( this.name );
		this.author = Optional.ofNullable( data.author ).orElse( this.author );
		this.ignored_entries.addAll( data.ignored_entries );
		// TODO: handle version check
	}
	
	@Nullable
	protected Object _loadJsonEntry(
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
		
		try { return ctx.loadContent( loader_entry, obj, build_context ); }
		catch ( LoaderNotFoundException e )
		{
			final String path = this.sourceName() + "/" + file_path;
			FMUM.logError( "fmum.type_loader_not_found", path, loader_entry );
		}
		return null;
	}
	
	protected static class PackMetadataTemplate
	{
		public String name;
		public String author;
		public Set< String > ignored_entries = Collections.emptySet();
	}
}
