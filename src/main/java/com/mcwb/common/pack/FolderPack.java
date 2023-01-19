package com.mcwb.common.pack;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import com.google.common.base.Supplier;

/**
 * Content packs that organized in folders
 * 
 * @author Giant_Salted_Fish
 */
public class FolderPack extends AbstractLocalPack
{
	public FolderPack( File source ) { super( source ); }
	
	@Override
	public void load()
	{
		// Read pack info if has
		final File infoFile = new File( this.source, this.infoFile() );
		if( infoFile.exists() )
		{
			try( FileReader in = new FileReader( infoFile ) ) { this.setupInfoWith( in ); }
			catch( IOException e ) {
				this.printError( this.sourceName() + "/" + this.infoFile(), e );
			}
		}
		
		// Load all types in rest folders except "assets/" folder
		for( final File dir : this.source.listFiles() )
		{
			final String dirName = dir.getName();
			if( dir.isDirectory() && !dirName.contentEquals( "assets" ) )
				this.tryLoadFrom( dir, this.getFallbackType( dirName ), () -> dirName );
		}
	}
	
	protected void tryLoadFrom( File dir, String fallbackType, Supplier< String > parentPath )
	{
		for( final File file : dir.listFiles() )
		{
			// Make sure all files are touched
			final String fName = file.getName();
			if( file.isDirectory() )
			{
				this.tryLoadFrom( file, fallbackType, () -> parentPath.get() + "/" + fName );
				continue;
			}
			
			final Supplier< String > sourceTrace = () -> this.sourceName()
				+ "/" + parentPath.get() + "/" + fName;
			try
			{
				// Load ".json" type file
				if( fName.endsWith( ".json") )
					try( FileReader in = new FileReader( file ) )
					{
						this.loadJsonType(
							in,
							fallbackType,
							fName.substring( 0, fName.length() - 5 ),
							sourceTrace
						);
					}
				
				// Load ".class" type file
				else if( fName.endsWith( ".class" ) )
					this.loadClassType( parentPath.get().replace( '/', '.' ) + "." + fName );
			}
			catch( Exception e ) { this.printError( sourceTrace.get(), e ); }
		}
	}
}
