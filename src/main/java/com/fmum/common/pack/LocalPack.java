package com.fmum.common.pack;

import com.fmum.common.FMUM;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import javax.annotation.Nullable;
import java.io.File;
import java.io.Reader;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public abstract class LocalPack implements IContentPack
{
	protected static final String ERROR_LOADING_INFO = "fmum.error_loading_pack_info";
	protected static final String ERROR_LOADING_TYPE = "fmum.error_loading_type";
	
	protected static final String META_FILE_PATH = "pack.json";
	
	protected final File source;
	protected final HashSet< String > ignored_entries = new HashSet<>();
	
	protected String name;
	protected String author = "fmum.author_missing";
	
	protected LocalPack( File source )
	{
		this.source = source;
		this.name = source.getName();
		this.ignored_entries.add( "assets" );
	}
	
	@Override
	public void prepareLoad( IPrepareContext ctx ) {
		ctx.regisResourceDomain( this.source );
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
		
		try { return ctx.loadType( loader_entry, obj ); }
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
