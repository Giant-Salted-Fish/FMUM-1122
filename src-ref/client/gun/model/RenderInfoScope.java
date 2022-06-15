package com.fmum.client.gun.model;

import com.fmum.common.util.ObjPool;

public class RenderInfoScope extends RenderInfoAimable
{
	/**
	 * A pool that buffers scope eyepiece textures
	 */
	protected static final ObjPool<Integer> scopeTexturePool = new ObjPool<>(
		() -> 1 // FIXME: initialize a texture
	);
	
	private static final ObjPool<RenderInfoScope> pool = new ObjPool<>(() -> new RenderInfoScope());
	
	public double scopeAlpha = 0D;
	public int scopeTexture = 0;
	
	protected RenderInfoScope() { }
	
	public static RenderInfoScope get() { return pool.poll(); }
	
	@Override
	public void release()
	{
		if(this.scopeTexture != 0)
		{
			scopeTexturePool.back(this.scopeTexture);
			this.scopeTexture = 0;
		}
		pool.back(this);
	}
}
