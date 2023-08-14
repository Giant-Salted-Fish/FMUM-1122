package com.fmum.common.pack;

import com.fmum.common.FMUM;
import com.fmum.common.pack.IPreparedPack.ILoadContext;
import net.minecraftforge.fml.common.ModContainer;

import java.io.File;
import java.io.FileReader;

/**
 * For content packs that organized in form of folders.
 *
 * @author Giant_Salted_Fish
 */
public class FolderPack extends LocalPack
{
	public FolderPack( ModContainer mod_container ) {
		super( mod_container );
	}
	
	@Override
	protected void _loadPackContent( ILoadContext ctx )
	{
		for ( final File dir : this.mod_container.getSource().listFiles() )
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
				FMUM.MOD.logException( e, ERROR_LOADING_TYPE, source_trace );
			}
		}
	}
}
