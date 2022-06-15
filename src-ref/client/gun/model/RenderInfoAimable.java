package com.fmum.client.gun.model;

import com.fmum.client.module.model.RenderInfoModule;
import com.fmum.common.util.ObjPool;
import com.fmum.common.util.Vec3;

public class RenderInfoAimable extends RenderInfoModule implements Comparable<RenderInfoAimable>
{
	private static final ObjPool<RenderInfoAimable>
		pool = new ObjPool<>(() -> new RenderInfoAimable());
	
	public final Vec3 reticleRefPos = Vec3.get(4096D, 0D, 0D);
	
	protected RenderInfoAimable() { }
	
	public static RenderInfoAimable get() { return pool.poll(); }
	
	@Override
	public void release() { pool.back(this); }
	
	@Override
	public int compareTo(RenderInfoAimable a) {
		return 0; // FIXME: sights with lower x coordinate sure be ahead in queue
	}
}
