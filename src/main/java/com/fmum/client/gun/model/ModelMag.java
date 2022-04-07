package com.fmum.client.gun.model;

import java.util.function.Consumer;

import org.lwjgl.opengl.GL11;

import com.fmum.client.module.model.ModelModular;
import com.fmum.common.util.Mesh;
import com.fmum.common.util.Vec3;

public class ModelMag extends ModelModular
{
	public static final byte
		FOLLOWER = 0,
		MAG = 1;
	
	protected static final Vec3[] DEF_POS_ROT = { new Vec3(0D) };
	
	public Vec3[] followerPos = DEF_POS_ROT;
	
	public ModelMag(Mesh follower, Mesh mag, Consumer<ModelMag> initializer)
	{
		super(follower, mag);
		
		initializer.accept(this);
	}
	
	@Override
	public void render()
	{
		for(int i = this.meshes.length; --i > FOLLOWER; this.meshes[i].render());
		
		Vec3 v = this.followerPos[0];
		GL11.glTranslated(v.x, v.y, v.z);
		this.meshes[FOLLOWER].render();
	}
}
