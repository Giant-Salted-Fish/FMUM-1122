package com.fmum.client.model.module;

import org.lwjgl.opengl.GL11;

import com.fmum.client.FMUMClient;
import com.fmum.client.ResourceManager;
import com.fmum.common.module.TypeModular;
import com.fmum.common.util.CoordSystem;
import com.fmum.common.util.ObjPool;
import com.fmum.common.util.Vec3;

import net.minecraft.nbt.NBTTagList;

public class ModuleRenderInfo
{
	public static final ObjPool<ModuleRenderInfo>
		pool = new ObjPool<>(() -> new ModuleRenderInfo());
	
	protected static final Vec3 vec = new Vec3();
	
	public NBTTagList tag = null;
	
	public TypeModular type = null;
	
	public final CoordSystem sys = new CoordSystem();
	
	public void render()
	{
		// Apply transform
		double[] v = this.sys.vec;
		GL11.glTranslated(
			v[CoordSystem.OFFSET + CoordSystem.X],
			v[CoordSystem.OFFSET + CoordSystem.Y],
			v[CoordSystem.OFFSET + CoordSystem.Z]
		);
		
		this.sys.getAngle(vec);
		GL11.glRotated(vec.y, 0D, 1D, 0D);
		GL11.glRotated(vec.z, 0D, 0D, 1D);
		GL11.glRotated(vec.x, 1D, 0D, 0D);
		
		double scale = this.type.modelScale;
		GL11.glScaled(scale, scale, scale);
		
		// Bind texture and render
		FMUMClient.mc.renderEngine.bindTexture(
			ResourceManager.getTexture(type.getTexture(this.tag))
		);
		this.type.model.render();
	}
	
	/**
	 * Called when this info instance is released
	 */
	public void release() { pool.back(this); }
}
