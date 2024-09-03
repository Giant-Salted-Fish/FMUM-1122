package com.fmum.load;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.function.Consumer;

/**
 * Enhanced content load context support backed up by each pack.
 */
public interface IContentBuildContext extends ILoadContext
{
	IPackInfo getPackInfo();
	
	void regisPostLoadCallback( Consumer< IPostLoadContext > callback );
	
	@SideOnly( Side.CLIENT )
	void regisMeshLoadCallback( Consumer< IMeshLoadContext > callback );
}
