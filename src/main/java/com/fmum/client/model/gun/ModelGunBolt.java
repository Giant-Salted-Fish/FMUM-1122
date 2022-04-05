package com.fmum.client.model.gun;

import com.fmum.client.model.module.ModelModular;
import com.fmum.common.util.Mesh;

public class ModelGunBolt extends ModelModular
{
	public static final byte
		BOLT = 0,
		CARRIER = 1;
	
	public ModelGunBolt(Mesh bolt, Mesh carrier)
	{
		this.meshes = new Mesh[] {
			bolt,
			carrier
		};
	}
	
	// TODO: render valid gun bolt
}
