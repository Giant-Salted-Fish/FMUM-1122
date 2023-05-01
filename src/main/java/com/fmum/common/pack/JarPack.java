package com.fmum.common.pack;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.function.Supplier;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class JarPack extends LocalPack
{
	public JarPack( File source ) { super( source ); }
	
	@Override
	public void load()
	{
		// Load pack info first if has.
		try ( ZipInputStream zipIn = new ZipInputStream( new FileInputStream( this.source ) ) )
		{
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
			this.logException( e, ERROR_LOADING_INFO, filePath );
		}
		
		try ( ZipInputStream zipIn = new ZipInputStream( new FileInputStream( this.source ) ) )
		{
			for ( ZipEntry e; ( e = zipIn.getNextEntry() ) != null; )
			{
				if ( e.isDirectory() ) { continue; }
				
				final String eName = e.getName();
				final int i = eName.indexOf( '/' );
				final boolean isInFolder = i != -1;
				if ( !isInFolder ) { continue; }
				
				final String entry = eName.substring( 0, i );
				final boolean isIgnoredFolder = this.ignoreEntires.contains( entry );
				if ( isIgnoredFolder ) { continue; }
				
				final Supplier< String > sourceTrace = () -> this.sourceName() + "/" + eName;
				try
				{
					if ( eName.endsWith( ".json" ) )
					{
						this.loadJsonType(
							new InputStreamReader( zipIn ),
							this.getFallbackType( entry ),
							eName.substring( eName.lastIndexOf( '/' ) + 1, eName.length() - 5 ),
							sourceTrace
						);
					}
					else if ( eName.endsWith( ".class" ) ) {
						this.loadClassType( eName.replace( '/', '.' ) );
					}
				}
				catch ( Exception ee ) {
					this.logException( ee, ERROR_LOADING_TYPE, sourceTrace.get() );
				}
			}
		}
		catch ( IOException e ) {
			this.logError( "An IO exception has occurred loading <%s>", this.sourceName() );
		}
	}
}
