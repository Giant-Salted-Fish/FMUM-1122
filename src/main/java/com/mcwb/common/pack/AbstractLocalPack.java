package com.mcwb.common.pack;

import java.io.File;
import java.io.Reader;
import java.util.HashMap;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mcwb.common.IAutowireLogger;
import com.mcwb.common.MCWB;
import com.mcwb.common.load.BuildableLoader;
import com.mcwb.common.meta.IMeta;
import com.mcwb.common.meta.Meta;

/**
 * Represents a content pack on local disk
 * 
 * @author Giant_Salted_Fish
 */
public abstract class AbstractLocalPack extends Meta implements IContentProvider, IAutowireLogger
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
//		mapper.put( "mags", "mag" );
		mapper.put( "paintjobs", "paintjob" );
		if( MCWB.MOD.isClient() )
			mapper.put( "key_binds", "key_bind" );
	}
	
	/**
	 * Source file of this pack on local disk
	 */
	protected final File source;
	
	/**
	 * Author of the pack. Usually read from the ".json" pack info file.
	 */
	protected String author = "mcwb.author_missing";
	
	protected AbstractLocalPack( File source )
	{
		this.name = source.getName();
		this.source = source;
	}
	
	@Override
	public void preLoad() { MCWB.MOD.regisResourceDomain( this.source ); }
	
	@Override
	public String author() { return this.author; }
	
	@Override
	public String sourceName() { return this.source.getName(); }
	
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
		// TODO: handle version check
	}
	
	protected String getFallbackType( String entry ) {
		return ENTRY_MAP.getOrDefault( entry, entry );
	}
	
	@Nullable
	protected IMeta loadJsonType(
		Reader in,
		String fallbackType,
		String name,
		Supplier< String > sourceTrace
	) {
		// Parse from input reader
		final JsonObject obj = MCWB.GSON.fromJson( in, JsonObject.class );
		final JsonElement eEntry = obj.get( "__type__" );
		
		// Try get required loader and load
		final String entry = eEntry != null ? eEntry.getAsString().toLowerCase() : fallbackType;
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
	
	protected void printError( String source, Exception e ) {
		this.except( e, "mcwb.unexpected_error", source ); // TODO: rename this maybe?
	}
}
