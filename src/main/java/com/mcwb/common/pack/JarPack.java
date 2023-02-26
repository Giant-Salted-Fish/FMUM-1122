package com.mcwb.common.pack;

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
		// Load pack info first if has
		try( ZipInputStream zipIn = new ZipInputStream( new FileInputStream( this.source ) ) )
		{
			final String infoFile = this.infoFile();
			for( ZipEntry e; ( e = zipIn.getNextEntry() ) != null; )
				if( e.getName().equals( infoFile ) )
					this.setupInfoWith( new InputStreamReader( zipIn ) );
		}
		catch( IOException e ) {
			this.except( e, ERROR_LOADING_INFO, this.sourceName() + "/" + this.infoFile() );
		}
		
		try( ZipInputStream zipIn = new ZipInputStream( new FileInputStream( this.source ) ) )
		{
			for( ZipEntry e; ( e = zipIn.getNextEntry() ) != null; )
			{
				if( e.isDirectory() ) continue;
				
				// Skip stuffs not in folder or in "assets/" folder
				final String eName = e.getName();
				final int i = eName.indexOf( '/' );
				if( i < 0 || eName.startsWith( "assets/" ) ) continue;
				
				final Supplier< String > sourceTrace = () -> this.sourceName() + "/" + eName;
				try
				{
					if( eName.endsWith( ".json" ) )
					{
						this.loadJsonType(
							new InputStreamReader( zipIn ),
							this.getFallbackType( eName.substring( i ) ),
							eName.substring( eName.lastIndexOf( '/' ), eName.length() - 5 ),
							sourceTrace
						);
					}
					else if( eName.endsWith( ".class" ) )
						this.loadClassType( eName.replace( '/', '.' ) );
				}
				catch( Exception ee ) { this.except( ee, ERROR_LOADING_TYPE, sourceTrace.get() ); }
			}
		}
		catch( IOException e ) {
			this.error( "An IO exception has occurred loading <%s>", this.sourceName() );
		}
	}
}
