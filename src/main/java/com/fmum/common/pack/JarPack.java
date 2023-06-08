package com.fmum.common.pack;

import com.fmum.common.FMUM;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.file.Files;
import java.util.function.Supplier;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class JarPack extends LocalPack
{
	public JarPack( File source ) { super( source ); }
	
	@Override
	public void load()
	{
		// Load pack info first if it exists.
		try (
			ZipInputStream zipIn = new ZipInputStream(
				Files.newInputStream( this.source.toPath() )
			)
		) {
			final String infoFile = this.infoFile();
			for ( ZipEntry e; ( e = zipIn.getNextEntry () ) != null; )
			{
				final boolean isInfoFile = e.getName().equals( infoFile );
				if ( isInfoFile ) { this.setupInfoWith( new InputStreamReader( zipIn ) ); }
			}
		}
		catch ( IOException e )
		{
			final String filePath = this.sourceName() + "/" + this.infoFile();
			FMUM.logException( e, ERROR_LOADING_INFO, filePath );
		}
		
		try (
			ZipInputStream zipIn = new ZipInputStream(
				Files.newInputStream( this.source.toPath() )
			)
		) {
			for ( ZipEntry e; ( e = zipIn.getNextEntry() ) != null; )
			{
				if ( e.isDirectory() ) { continue; }
				
				final String eName = e.getName();
				final int i = eName.indexOf( '/' );
				final boolean isInFolder = i != -1;
				if ( !isInFolder ) { continue; }
				
				final String entry = eName.substring( 0, i );
				final boolean isIgnoredFolder = this.ignoredEntries.contains( entry );
				if ( isIgnoredFolder ) { continue; }
				
				final Supplier< String > sourceTrace = () -> this.sourceName() + "/" + eName;
				try
				{
					if ( eName.endsWith( ".json" ) )
					{
						final Reader in = new InputStreamReader( zipIn );
						final String fallbackType = entry;
						final int nameHead = eName.lastIndexOf( '/' ) + 1;
						final int nameEnd = eName.length() - ".json".length();
						final String name = eName.substring( nameHead,  nameEnd );
						this.loadJsonType( in, fallbackType, name, sourceTrace );
					}
					else if ( eName.endsWith( ".class" ) ) {
						this.loadClassType( eName.replace( '/', '.' ) );
					}
				}
				catch ( Exception ee ) {
					FMUM.logException( ee, ERROR_LOADING_TYPE, sourceTrace.get() );
				}
			}
		}
		catch ( IOException e ) {
			FMUM.logError( "An IO exception has occurred loading <%s>", this.sourceName() );
		}
	}
}
