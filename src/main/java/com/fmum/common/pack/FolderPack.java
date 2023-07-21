package com.fmum.common.pack;

import com.fmum.common.FMUM;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * For content packs that organized in form of folders.
 *
 * @author Giant_Salted_Fish
 */
public class FolderPack extends LocalPack
{
	public FolderPack( File source ) {
		super( source );
	}
	
	@Override
	protected void _loadContent( ILoadContext ctx )
	{
		// Read pack metadata first if it exists.
		final File meta_file = new File( this.source, META_FILE_PATH );
		if ( meta_file.exists() )
		{
			try ( FileReader in = new FileReader( meta_file ) )
			{
				final PackMetadataTemplate data = ctx.gson().
													 fromJson( in, PackMetadataTemplate.class );
				this._setupMetaDataWith( data );
			}
			catch ( IOException e ) {
				FMUM.logException( e, ERROR_LOADING_INFO, this.sourceName() + "/pack.json" );
			}
		}
		
		// Load all types in rest folder except the ignored ones(for example, "assets/" folder).
		for ( final File dir : this.source.listFiles() )
		{
			final String dir_name = dir.getName();
			if ( dir.isDirectory() && !this.ignored_entries.contains( dir_name ) )
			{
				final String fallback_type = dir_name;
				this._tryLoadFrom( dir, fallback_type, dir_name, ctx );
			}
		}
	}
	
	protected void _tryLoadFrom(
		File search_in_dir,
		String fallback_type,
		String parent_path,
		ILoadContext ctx
	) {
		for ( final File file : search_in_dir.listFiles() )
		{
			final String file_name = file.getName();
			final String file_path = parent_path + "/" + file_name;
			if ( file.isDirectory() )
			{
				this._tryLoadFrom( file, fallback_type, file_path, ctx );
				continue;
			}
			
			try
			{
				if ( file_name.endsWith( ".json" ) )
				{
					try ( FileReader in = new FileReader( file ) ) {
						this._loadJsonEntry( in, fallback_type, file_path, ctx );
					}
				}
				
//				else if ( file_name.endsWith( ".class" ) )
//				{
//					final String class_path = file_path.replace( '/', '.' );
//					this._loadClassEntry( class_path, context );
//				}
			}
			catch ( Exception e )
			{
				final String source_trace = this.sourceName() + "/" + file_path;
				FMUM.logException( e, ERROR_LOADING_TYPE, source_trace );
			}
		}
	}
}
