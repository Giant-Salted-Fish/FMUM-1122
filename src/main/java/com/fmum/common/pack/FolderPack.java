package com.fmum.common.pack;

import com.fmum.common.FMUM;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.function.Supplier;

/**
 * Content packs that organized as folders.
 * 
 * @author Giant_Salted_Fish
 */
public class FolderPack extends LocalPack
{
	public FolderPack( File source ) { super( source ); }
	
	@Override
	public void load()
	{
		// Read pack info first if it exists.
		final File infoFile = new File( this.source, this.infoFile() );
		if ( infoFile.exists() )
		{
			try ( FileReader in = new FileReader( infoFile ) ) { this.setupInfoWith( in ); }
			catch ( IOException e ) {
				FMUM.logException( e, ERROR_LOADING_INFO, this.sourceName() + "/" + this.infoFile() );
			}
		}
		
		// Load all types in rest folders except "assets/" folder.
		for ( final File dir : this.source.listFiles() )
		{
			final String dirName = dir.getName();
			if ( dir.isDirectory() && !this.ignoredEntries.contains( dirName ) )
			{
				final String fallbackType = dirName;
				final Supplier< String > sourceTrace = () -> dirName;
				this.tryLoadFrom( dir, fallbackType, sourceTrace );
			}
		}
	}
	
	protected void tryLoadFrom( File dir, String fallbackType, Supplier< String > parentPath )
	{
		for ( final File file : dir.listFiles() )
		{
			final String fName = file.getName();
			if ( file.isDirectory() )
			{
				this.tryLoadFrom( file, fallbackType, () -> parentPath.get() + "/" + fName );
				continue;
			}
			
			final Supplier< String > sourceTrace =
				() -> this.sourceName() + "/" + parentPath.get() + "/" + fName;
			try
			{
				if ( fName.endsWith( ".json" ) )
				{
					try ( FileReader in = new FileReader( file ) )
					{
						final String name = fName.substring( 0, fName.length() - 5 );
						this.loadJsonType( in, fallbackType, name, sourceTrace );
					}
				}
				else if ( fName.endsWith( ".class" ) )
				{
					final String classPath = parentPath.get().replace( '/', '.' ) + "." + fName;
					this.loadClassType( classPath );
				}
			}
			catch ( Exception e ) { FMUM.logException( e, ERROR_LOADING_TYPE, sourceTrace.get() ); }
		}
	}
}
