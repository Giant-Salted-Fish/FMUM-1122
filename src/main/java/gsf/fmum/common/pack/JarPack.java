package gsf.fmum.common.pack;

import gsf.fmum.common.FMUM;
import net.minecraftforge.fml.common.ModContainer;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.file.Files;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class JarPack extends LocalPack
{
	public JarPack( ModContainer mod_container ) {
		super( mod_container );
	}
	
	@Override
	protected void _loadPackContent( ILoadContext ctx )
	{
		try (
			ZipInputStream in = new ZipInputStream(
				Files.newInputStream( this.mod_container.getSource().toPath() ) )
		) {
			for ( ZipEntry e; ( e = in.getNextEntry() ) != null; )
			{
				if ( e.isDirectory() ) {
					continue;
				}
				
				final String file_path = e.getName();
				final int i = file_path.indexOf( '/' );
				final boolean not_in_folder = i < 0;
				if ( not_in_folder ) {
					continue;
				}
				
				final String entry = file_path.substring( 0, i );
				final boolean is_ignored_entry =
					this.ignored_entries.contains( entry );
				if ( is_ignored_entry ) {
					continue;
				}
				
				try
				{
					if ( file_path.endsWith( ".json" ) )
					{
						final Reader reader = new InputStreamReader( in );
						final String fallback_type = entry;
						this._loadJsonEntry(
							reader, fallback_type, file_path, ctx );
					}
					
					else if ( file_path.endsWith( ".class" ) )
					{
					
					}
				}
				catch ( Exception e_ )
				{
					final String err_src = this.sourceName() + "/" + file_path;
					FMUM.MOD.logException( e_, ERROR_LOADING_TYPE, err_src );
				}
			}
		}
		catch ( IOException e )
		{
			final String err_msg = "An IO exception has occurred loading <%s>";
			FMUM.MOD.logException( e, err_msg, this.sourceName() );
		}
	}
}
