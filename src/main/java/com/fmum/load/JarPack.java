package com.fmum.load;

import com.fmum.FMUM;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * A pack that loads resources from a zip (.jar) file.
 *
 * @see FolderPack
 */
public class JarPack extends LocalPack
{
	protected JarPack( File source, IPackInfo pack_info ) {
		super( source, pack_info );
	}
	
	@Override
	public void onLoad( ILoadContext ctx )
	{
		try ( ZipFile file = new ZipFile( this.source ) )
		{
			final IContentBuildContext build_ctx = this._buildContext( ctx );
			
			// Load key bindings if exists.
			FMUM.SIDE.runIfClient( () -> {
				final ZipEntry kb_entry = file.getEntry( "key_binding.json" );
				if ( kb_entry != null )
				{
					try ( InputStream in = file.getInputStream( kb_entry ) ) {
						this._loadKeyBinding( in, build_ctx );
					}
					catch ( IOException e ) {
						throw new RuntimeException( "Error load key bindings.", e );
					}
				}
			} );
			
			file.stream()
				.filter( ze -> !ze.isDirectory() )
				.forEachOrdered( ze -> {
					final String file_path = ze.getName();
					final boolean not_json_file = !file_path.endsWith( ".json" );
					if ( not_json_file ) {
						return;
					}
					
					final int i = file_path.indexOf( '/' );
					final boolean not_in_folder = i < 0;
					if ( not_in_folder ) {
						return;
					}
					
					final String entry = file_path.substring( 0, i );
					final boolean is_assets_entry = entry.equals( "assets" );
					if ( is_assets_entry ) {
						return;
					}
					
					// Parse fallback name.
					final int start = file_path.lastIndexOf( '/' ) + 1;
					final int end = file_path.length() - ".json".length();
					final String fallback_name = file_path.substring( start, end );
					
					try ( InputStream in = file.getInputStream( ze ) )
					{
						final InputStreamReader reader = new InputStreamReader( in );
						this._loadJsonEntry( reader, entry, fallback_name, build_ctx )
							.matchAnyError( this._logLoadError( file_path ) )
							.exhaustive();
					}
					catch ( IOException e ) {
						throw new RuntimeException( e );
					}
				} );
		}
		catch ( IOException e ) {
			throw new RuntimeException( e );
		}
	}
}
