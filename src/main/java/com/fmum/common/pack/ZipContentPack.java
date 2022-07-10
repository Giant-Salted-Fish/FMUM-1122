package com.fmum.common.pack;

import java.io.File;
import java.io.IOException;
import java.util.TreeMap;

import com.fmum.common.meta.EnumMeta;
import com.fmum.common.meta.MetaBase;
import com.fmum.common.util.Messager;

/**
 * For content packs that are packed into .zip or .jar file.
 * 
 * @author Giant_Salted_Fish
 */
public final class ZipContentPack extends LocalContentProvider
{
	private static final TreeMap< String, EnumMeta > TYPE_MAP = new TreeMap<>();
	static
	{
		for( EnumMeta type : EnumMeta.values() )
			TYPE_MAP.put( type.recommendedSourceDirName, type );
	}
	
	public ZipContentPack( File zip ) { super( zip ); }
	
	@Override
	public void loadContent()
	{
		// Load pack info
		this.iterateZipEntries(
			this.source,
			( entry, in ) -> {
				if( !ContentPackInfo.RECOMMENDED_FILE_NAME.equals( entry.getName() ) )
					return false;
				
				this.parseInfo( in, () -> this.sourceName() + ":" + entry.getName() );
				return true;
			}
		);
		
		// Load contents
		this.iterateZipEntries(
			this.source,
			( entry, in ) -> {
				// Skip folders
				if( entry.isDirectory() ) return false;
				
				// Ignore entries that are not in a type folder
				final String entryName = entry.getName();
				final int i = entryName.indexOf( '/' );
				if( i < 0 ) return false;
				
				// Skip if the folder name does not corresponds to a meta type
				EnumMeta type = TYPE_MAP.get( entryName.substring( 0, i ) );
				if( type == null ) return false;
				
				final String fName = entryName.substring( entryName.lastIndexOf( '/' ) + 1);
				final Messager sourceTrace = () -> this.sourceName() + ":" + entryName;
				
				// Load type by file type
				MetaBase meta = null;
				if( entryName.endsWith( ".txt" ) )
					try
					{
						meta = type.parser.parse(
							in,
							fName.substring( 0, fName.length() - ".txt".length() ),
							sourceTrace
						);
					}
					catch( IOException e ) { this.printIOError( sourceTrace.message(), e ); }
					catch( Exception e ) { this.printUnexpectedError( sourceTrace.message(), e ); }
				else if( entryName.endsWith( ".class" ) )
					meta = this.loadClassBasedMeta( sourceTrace, entryName.replace( '/', '.' ) );
				else this.printUnknownFileType( sourceTrace.message() );
				
				if( meta != null )
				{
					meta.$provider( this );
					meta.onPostInit();
				}
				
				return false;
			}
		);
	}
}
