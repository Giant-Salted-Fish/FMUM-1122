package com.fmum.load;

import com.fmum.render.ModelPath;
import gsf.util.render.Mesh;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Optional;

@FunctionalInterface
@SideOnly( Side.CLIENT )
public interface IMeshLoadContext
{
	/**
	 * Loaded meshes will be buffered to avoid unnecessary copies of the same
	 * mesh.
	 */
	Optional< Mesh > loadMesh( ModelPath path );
}
