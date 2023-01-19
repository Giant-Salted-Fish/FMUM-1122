package com.mcwb.common.pack;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.function.Supplier;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class JarPack extends AbstractLocalPack
{
	public JarPack( File source ) { super( source ); }
	
	@Override
	public void load()
	{
		// Load pack info
		try( ZipInputStream zipIn = new ZipInputStream( new FileInputStream( this.source ) ) )
		{
			final String infoFile = this.infoFile();
			for( ZipEntry e; ( e = zipIn.getNextEntry() ) != null; )
				if( e.getName().equals( infoFile ) )
					this.setupInfoWith( new InputStreamReader( zipIn ) );
		}
		catch( IOException e ) { this.printError( this.sourceName() + "/" + this.infoFile(), e ); }
		
		// Do actual load types
		try( ZipInputStream zipIn = new ZipInputStream( new FileInputStream( this.source ) ) )
		{
			for( ZipEntry e; ( e = zipIn.getNextEntry() ) != null; )
			{
				// Skip folders
				if( e.isDirectory() )
					continue;
				
				// Skip stuffs not in folder or in "assets/" folder
				final String eName = e.getName();
				final int i = eName.indexOf( '/' );
				if( i < 0 || eName.startsWith( "assets/" ) )
					continue;
				
				final Supplier< String > sourceTrace = () -> this.sourceName() + "/" + eName;
				try
				{
					// Handle ".json" type file
					if( eName.endsWith( ".json" ) )
						this.loadJsonType(
							new InputStreamReader( zipIn ),
							this.getFallbackType( eName.substring( i ) ),
							eName.substring( eName.lastIndexOf( '/' ), eName.length() - 5 ),
							sourceTrace
						);
					
					// Handle ".class" type file
					else if( eName.endsWith( ".class" ) )
						this.loadClassType( eName.replace( '/', '.' ) );
				}
				catch( Exception ee ) { this.printError( sourceTrace.get(), ee ); }
			}
		}
		catch( IOException e ) { this.printError( this.sourceName(), e ); }
	}
}
