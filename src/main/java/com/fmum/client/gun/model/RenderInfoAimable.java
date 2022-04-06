package com.fmum.client.gun.model;

import com.fmum.client.module.model.RenderInfoModule;
import com.fmum.common.util.ObjPool;
import com.fmum.common.util.Vec3;

public class RenderInfoAimable extends RenderInfoModule implements Comparable<RenderInfoAimable>
{
	public static final ObjPool<RenderInfoAimable>
		pool = new ObjPool<>(() -> new RenderInfoAimable());
	
	public final Vec3 reticleRefPos = new Vec3(4096D, 0D, 0D);
	
	@Override
	public void release() { pool.back(this); }
	
	@Override
	public int compareTo(RenderInfoAimable a) {
		return 0; // TODO
	}
}
