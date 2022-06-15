package com.fmum.client.gun.model;

import com.fmum.client.module.model.ModelModular;
import com.fmum.common.util.Mesh;

public class ModelGunBolt extends ModelModular
{
	public static final byte
		BOLT = 0,
		CARRIER = 1;
	
	public ModelGunBolt(Mesh bolt, Mesh carrier) { super(bolt, carrier); }
	
	// TODO: render valid gun bolt
}
