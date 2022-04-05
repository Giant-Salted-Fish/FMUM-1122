package com.fmum.client.model.module;

import java.util.function.Consumer;

import com.fmum.client.model.ModelMeshBased;
import com.fmum.common.module.TypeModular;
import com.fmum.common.util.CoordSystem;
import com.fmum.common.util.Mesh;

import net.minecraft.nbt.NBTTagList;

public class ModelModular extends ModelMeshBased
{
	public ModelModular() { }
	
	public ModelModular(Mesh... meshes) { super(meshes); }
	
	public ModelModular(Consumer<ModelModular> initializer) { initializer.accept(this); }
	
	// TODO: override in aimable and scope model 
	public RenderInfoModule prepareRenderInfo(NBTTagList tag, TypeModular type, CoordSystem sys)
	{
		RenderInfoModule info = this.getRenderInfo();
		info.tag = tag;
		info.type = type;
		info.sys.set(sys);
		return info;
	}
	
	protected RenderInfoModule getRenderInfo() { return RenderInfoModule.pool.poll(); }
}
