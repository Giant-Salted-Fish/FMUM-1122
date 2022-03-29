package com.fmum.client.model.gun;

import com.fmum.common.util.ObjPool;

public class ScopeRenderInfo extends AimableRenderInfo
{
	public static final ObjPool<ScopeRenderInfo> pool = new ObjPool<>(() -> new ScopeRenderInfo());
	
	public double scopeAlpha = 0D;
	public int scopeTexture = 0;
	
	@Override
	public void release() { pool.back(this); }
}
