package com.fmum.common.load;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * You may consider this class as client only, but in fact many types may inherit this to load their
 * meshes and that could crash the game on physical server side.
 * 
 * @author Giant_Salted_Fish
 */
@FunctionalInterface
public interface IMeshLoadSubscriber
{
	/**
	 * Called on world load to load the meshes.
	 * 
	 * @see IContentProvider#regisMeshLoadSubscriber(IMeshLoadSubscriber)
	 */
	@SideOnly( Side.CLIENT )
	void onMeshLoad() throws Exception;
}
