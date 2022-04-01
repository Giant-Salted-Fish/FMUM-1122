package com.fmum.client.model.gun;

import com.fmum.common.util.ObjPool;

public class RenderInfoScope extends RenderInfoAimable
{
	public static final ObjPool<RenderInfoScope> pool = new ObjPool<>(() -> new RenderInfoScope());
	
	public double scopeAlpha = 0D;
	public int scopeTexture = 0;
	
	@Override
	public void release() { pool.back(this); }
}
