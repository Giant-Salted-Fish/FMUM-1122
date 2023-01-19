package com.mcwb.common.load;

import com.mcwb.common.pack.IContentProvider;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * This actually should be {@link Side#CLIENT} only. But it is not considering the fact that content
 * creators may create their types that inherit this interface and that could crash the load on
 * {@link Side#SERVER}.
 * 
 * @author Giant_Salted_Fish
 */
@FunctionalInterface
public interface IRequireMeshLoad
{
	/**
	 * Called on world load to compile the meshes
	 * 
	 * @see IContentProvider#regisMeshLoad(RequireMeshLoad)
	 */
	@SideOnly( Side.CLIENT )
	public void onMeshLoad() throws Exception;
}
