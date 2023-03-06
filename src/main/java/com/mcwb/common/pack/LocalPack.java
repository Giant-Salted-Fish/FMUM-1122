package com.mcwb.common.pack;

import java.io.File;
import java.io.Reader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mcwb.common.IAutowireLogger;
import com.mcwb.common.MCWB;
import com.mcwb.common.load.BuildableLoader;
import com.mcwb.common.load.IContentProvider;
import com.mcwb.common.meta.IMeta;
import com.mcwb.common.meta.Meta;

/**
 * Represents a content pack on local disk
 * 
 * @author Giant_Salted_Fish
 */
public abstract class LocalPack extends Meta implements IContentProvider, IAutowireLogger
{
	/**
	 * Map folder entries to proper type loaders
	 */
	protected static final HashMap< String, String > ENTRY_MAP = new HashMap<>();
	static
	{
		final HashMap< String, String > mapper = ENTRY_MAP;
		mapper.put( "creative_tabs", "creative_tab" );
		mapper.put( "guns", "gun" );
		mapper.put( "gun_parts", "gun_part" );
		mapper.put( "mags", "mag" );
		mapper.put( "paintjobs", "paintjob" );
		mapper.put( "key_binds", "key_bind" );
	}
	
	protected static final String ERROR_LOADING_INFO = "mcwb.error_loading_pack_info";
	protected static final String ERROR_LOADING_TYPE = "mcwb.error_loading_type";
	
	/**
	 * Source file of this pack on local disk
	 */
	protected final File source;
	
	/**
	 * Author of the pack. Usually read from the ".json" pack info file.
	 */
	protected String author = "mcwb.author_missing";
	
	protected final HashSet< String > ignoreEntires = new HashSet<>();
	
	protected LocalPack( File source )
	{
		this.name = source.getName();
		this.source = source;
		this.ignoreEntires.add( "assets" );
	}
	
	@Override
	public void preLoad() { MCWB.MOD.addResourceDomain( this.source ); }
	
	@Override
	public String author() { return this.author; }
	
	@Override
	public String sourceName() { return this.source.getName(); }
	
	/**
	 * @return Path of the pack info file to load
	 */
	protected String infoFile() { return "pack.json"; }
	
	/**
	 * This is required to be complete before the actual type load to ensure the integrity of the
	 * pack's information
	 */
	protected void setupInfoWith( Reader in )
	{
		final JsonObject obj = MCWB.GSON.fromJson( in, JsonObject.class );
		if( obj.has( "name" ) )
			this.name = obj.get( "name" ).getAsString();
		if( obj.has( "author" ) )
			this.author = obj.get( "author" ).getAsString();
		if( obj.has( "ignoreEntries" ) )
		{
			final JsonArray arr = obj.get( "ignoreEntries" ).getAsJsonArray();
			for( int i = arr.size(); i-- > 0; )
				this.ignoreEntires.add( arr.get( i ).getAsString() );
		}
		// TODO: handle version check
	}
	
	protected String getFallbackType( String entry ) {
		return ENTRY_MAP.getOrDefault( entry, entry );
	}
	
	/**
	 * @return {@code null} if fails to find proper loader
	 */
	@Nullable
	protected IMeta loadJsonType(
		Reader in,
		String fallbackType,
		String name,
		Supplier< String > sourceTrace
	) {
		final JsonObject obj = MCWB.GSON.fromJson( in, JsonObject.class );
		
		// Check if it has specified its type
		final JsonElement type = obj.get( "__type__" );
		final String entry = type != null ? type.getAsString().toLowerCase() : fallbackType;
		final BuildableLoader< ? extends IMeta > loader = MCWB.TYPE_LOADERS.get( entry );
		if( loader != null )
			return loader.parser.apply( obj ).build( name, this );
		
		this.error( "mcwb.type_loader_not_found", sourceTrace.get(), entry );
		return null;
	}
	
	protected IMeta loadClassType( String filePath ) throws Exception
	{
		// Remove ".class" suffix
		final String path = filePath.substring( 0, filePath.length() - 6 );
		return ( IMeta ) MCWB.MOD.loadClass( path )
			.getConstructor( String.class, IContentProvider.class )
			.newInstance( path.substring( Math.min( 0, path.lastIndexOf( '.' ) ) ), this );
	}
}
