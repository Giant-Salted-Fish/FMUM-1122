package com.fmum.common.pack;

import com.fmum.common.FMUM;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.file.Files;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class JarPack extends LocalPack
{
	public JarPack( File source ) {
		super( source );
	}
	
	@Override
	protected void _loadContent( ILoadContext ctx )
	{
		// Load pack metadata if exists.
		try (
			ZipInputStream in = new ZipInputStream(
				Files.newInputStream( this.source.toPath() ) )
		) {
			for ( ZipEntry e; ( e = in.getNextEntry() ) != null; )
			{
				if ( e.getName().equals( META_FILE_PATH ) )
				{
					final Reader reader = new InputStreamReader( in );
					final PackMetadataTemplate data = ctx.gson()
						.fromJson( reader, PackMetadataTemplate.class );
					this._setupMetaDataWith( data );
					break;
				}
			}
		}
		catch ( IOException e )
		{
			final String file_path = this.sourceName() + "/" + META_FILE_PATH;
			FMUM.logException( e, ERROR_LOADING_INFO, file_path );
		}
		
		try (
			ZipInputStream in = new ZipInputStream(
				Files.newInputStream( this.source.toPath() ) )
		) {
			for ( ZipEntry e; ( e = in.getNextEntry() ) != null; )
			{
				if ( e.isDirectory() ) {
					continue;
				}
				
				final String file_path = e.getName();
				final int i = file_path.indexOf( '/' );
				final boolean not_in_folder = i < 0;
				if ( not_in_folder ) {
					continue;
				}
				
				final String entry = file_path.substring( 0, i );
				final boolean is_ignored_entry = this.ignored_entries.contains( entry );
				if ( is_ignored_entry ) {
					continue;
				}
				
				try
				{
					if ( file_path.endsWith( ".json" ) )
					{
						final Reader reader = new InputStreamReader( in );
						final String fallback_type = entry;
						this._loadJsonEntry( reader, fallback_type, file_path, ctx );
					}
				}
				catch ( Exception e_ )
				{
					final String source_trace = this.sourceName() + "/" + file_path;
					FMUM.logException( e_, ERROR_LOADING_TYPE, source_trace );
				}
			}
		}
		catch ( IOException e ) {
			FMUM.logError( "An IO exception has occurred loading <%s>", this.sourceName() );
		}
	}
}
