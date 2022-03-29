package com.fmum.client.model.module;

import com.fmum.client.model.MeshBasedModel;
import com.fmum.common.module.TypeModular;
import com.fmum.common.util.CoordSystem;
import com.fmum.common.util.Mesh;

import net.minecraft.nbt.NBTTagList;

public class ModelModular extends MeshBasedModel
{
	public ModelModular() { }
	
	public ModelModular(Mesh mesh) { super(mesh); }
	
	public ModelModular(Mesh[] meshes) { super(meshes); }
	
	// TODO: override in aimable and scope model 
	public ModuleRenderInfo prepareRenderInfo(NBTTagList tag, TypeModular type, CoordSystem sys)
	{
		ModuleRenderInfo info = this.getRenderInfo();
		info.tag = tag;
		info.type = type;
		info.sys.set(sys);
		return info;
	}
	
	protected ModuleRenderInfo getRenderInfo() { return ModuleRenderInfo.pool.poll(); }
}
