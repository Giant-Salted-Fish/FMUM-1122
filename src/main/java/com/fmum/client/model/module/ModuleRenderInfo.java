package com.fmum.client.model.module;

import com.fmum.common.module.TypeModular;
import com.fmum.common.util.CoordSystem;
import com.fmum.common.util.ObjPool;

import net.minecraft.nbt.NBTTagList;

public class ModuleRenderInfo
{
	public static final ObjPool<ModuleRenderInfo>
		pool = new ObjPool<>(() -> new ModuleRenderInfo());
	
	public NBTTagList tag = null;
	
	public TypeModular type = null;
	
	// TODO: validate if necessary?
	public ModelModular model = null;
	
	public final CoordSystem sys = new CoordSystem();
	
	/**
	 * Called when this info instance is released
	 */
	public void release() { pool.back(this); }
}
