package com.fmum.client.model.gun;

import com.fmum.client.model.module.RenderInfoModule;
import com.fmum.common.util.ObjPool;
import com.fmum.common.util.Vec3;

public class RenderInfoAimable extends RenderInfoModule
{
	public static final ObjPool<RenderInfoAimable>
		pool = new ObjPool<>(() -> new RenderInfoAimable());
	
	public final Vec3 reticleRefPos = new Vec3(4096D, 0D, 0D);
	
	@Override
	public void release() { pool.back(this); }
}
