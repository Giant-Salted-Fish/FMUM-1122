package com.fmum.common.pack;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import com.fmum.common.FMUM;
import com.fmum.common.meta.EnumMeta;
import com.fmum.common.meta.MetaBase;
import com.fmum.common.util.LocalAttrParser;
import com.fmum.common.util.Messager;

/**
 * Content packs that are in form of folder in {@link FMUM#packDirName}
 * 
 * @author Giant_Salted_Fish
 */
public final class FolderContentPack extends LocalContentProvider
{
	/**
	 * Parser that is currently used to parse plain type file
	 */
	protected LocalAttrParser< ? extends MetaBase > curParser = null;
	
	public FolderContentPack( File dir ) { super( dir ); }
	
	@Override
	public void loadContent()
	{
		// Read pack info
		final File info = new File( this.source, ContentPackInfo.RECOMMENDED_FILE_NAME );
		final Messager sourceTrace = () -> this.source.getName() + "." + info.getName();
		if( info.exists() )
			try( BufferedReader in = new BufferedReader( new FileReader( info ) ) ) {
				this.parseInfo( in, sourceTrace );
			}
			catch( IOException e ) { this.printIOError( sourceTrace.message(), e ); }
		
		// Iterate through each type folder
		for( EnumMeta type : EnumMeta.values() )
		{
			final File dir = new File( this.source, type.recommendedSourceDirName );
			if( !dir.exists() || !dir.isDirectory() ) continue;
			
			// Set parser before processing type files
			this.curParser = type.parser;
			this.iterateTypeFiles(
				dir,
				( typeFile, superClassPath ) -> {
					final String fName = typeFile.getName();
					final Messager fileTrace
						= () -> this.sourceName() + "." + superClassPath + "." + fName;
					
					MetaBase meta = null;
					if( fName.endsWith( ".txt" ) )
						try
						{
							meta = this.curParser.parse(
								typeFile,
								fName.substring( 0, fName.length() - ".txt".length() ),
								fileTrace
							);
						}
						catch( IOException e ) { this.printIOError( fileTrace.message(), e ); }
					
					// Load type from class file
					else if( fName.endsWith( ".class" ) )
						meta = this.loadClassBasedMeta( fileTrace, superClassPath, fName );
					else this.printUnknownFileType( fileTrace.message() );
					
					if( meta != null )
					{
						meta.$provider( this );
						meta.onPostInit();
					}
					
					// Always go through next file
					return false;
				}
			);
		}
	}
}
