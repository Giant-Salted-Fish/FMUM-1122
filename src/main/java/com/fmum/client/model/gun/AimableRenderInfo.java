package com.fmum.client.model.gun;

import com.fmum.client.model.module.ModuleRenderInfo;
import com.fmum.common.util.ObjPool;
import com.fmum.common.util.Vec3;

public class AimableRenderInfo extends ModuleRenderInfo
{
	public static final ObjPool<AimableRenderInfo>
		pool = new ObjPool<>(() -> new AimableRenderInfo());
	
	public final Vec3 reticleRefPos = new Vec3(4096D, 0D, 0D);
	
	@Override
	public void release() { pool.back(this); }
}
