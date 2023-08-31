package com.fmum.common.pack;

import net.minecraftforge.fml.common.ModContainer;

import java.io.File;

/**
 * For content packs that organized in form of folders.
 */
public class FolderPack extends LocalPack
{
	public FolderPack( ModContainer mod_container ) {
		super( mod_container );
	}
	
	@Override
	protected void _loadPackContent( ILoadContext ctx )
	{
		for ( File dir : this.mod_container.getSource().listFiles() )
		{
			final String dir_name = dir.getName();
			if ( dir.isDirectory() && !this.ignored_entries.contains( dir_name ) )
			{
				final String fallback_type = dir_name;
				this._tryLoadFromDir( dir, fallback_type, dir_name, ctx );
			}
		}
	}
}
