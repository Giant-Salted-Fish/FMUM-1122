package com.fmum.load;

import com.fmum.FMUM;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Objects;

/**
 * This exists mainly for the development environment.
 *
 * @see JarPack
 */
public class FolderPack extends LocalPack
{
	public FolderPack( File source, IPackInfo pack_info ) {
		super( source, pack_info );
	}
	
	@Override
	public void onLoad( ILoadContext ctx )
	{
		final IContentBuildContext build_ctx = this._buildContext( ctx );
		
		// Load key bindings if exists.
		FMUM.SIDE.runIfClient( () -> {
			final File kb_file = new File( this.source, "key_binding.json" );
			if ( kb_file.exists() )
			{
				final Path path = kb_file.toPath();
				try ( InputStream in = Files.newInputStream( path ) ) {
					this._loadKeyBinding( in, build_ctx );
				}
				catch ( IOException e ) {
					throw new RuntimeException( "Error load key bindings.", e );
				}
			}
		} );
		
		Arrays.stream( Objects.requireNonNull( this.source.listFiles() ) )
			.filter( File::isDirectory )
			.filter( dir -> !dir.getName().equals( "assets" ) )
			.forEachOrdered( dir -> {
				final String dir_name = dir.getName();
				this._loadFromDir( dir, dir_name, dir_name, build_ctx );
			} );
	}
	
	protected void _loadFromDir(
		File search_in_dir,
		String fallback_type,
		String parent_path,
		IContentBuildContext ctx
	) {
		final File[] files = Objects.requireNonNull( search_in_dir.listFiles() );
		Arrays.stream( files )  // Maybe better use partition?
			.filter( File::isDirectory )
			.forEachOrdered( dir -> {
				final String dir_path = Paths.get( parent_path, dir.getName() ).toString();
				this._loadFromDir( dir, fallback_type, dir_path, ctx );
			} );
		
		Arrays.stream( files )
			.filter( File::isFile )
			.filter( f -> f.getName().endsWith( ".json" ) )
			.forEachOrdered( file -> {
				// Parse fallback name.
				final String file_name = file.getName();
				final int end = file_name.length() - ".json".length();
				final String fallback_name = file_name.substring( 0, end );
				
				try ( FileReader in = new FileReader( file ) )
				{
					this._loadJsonEntry( in, fallback_type, fallback_name, ctx )
						.matchAnyError( this._logLoadError( parent_path, file_name ) )
						.exhaustive();
				}
				catch ( IOException e ) {
					throw new RuntimeException( e );
				}
			} );
	}
}
