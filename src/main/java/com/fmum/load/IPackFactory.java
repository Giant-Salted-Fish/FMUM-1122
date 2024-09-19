package com.fmum.load;

import com.fmum.FMUM;
import com.mojang.realmsclient.util.Pair;
import net.minecraftforge.fml.common.ModContainer;

import java.io.File;

/**
 * Any mod that depends on {@link FMUM} should implement this to provide the
 * content pack that it holds to be load.
 */
public interface IPackFactory
{
	default Pair< IPackInfo, IPackLoadCallback > create( ModContainer container )
	{
		// FolderPack is used in development environment.  // TODO: Validate this?
		final IPackInfo pack_info = IPackInfo.of( container );
		final File source = container.getSource();
		final IPackLoadCallback load_callback = (
			source.isFile()
			? new JarPack( source, pack_info )
			: new FolderPack( source, pack_info )
		);
		return Pair.of( pack_info, load_callback );
	}
}
