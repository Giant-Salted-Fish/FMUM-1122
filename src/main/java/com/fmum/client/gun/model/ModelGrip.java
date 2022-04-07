package com.fmum.client.gun.model;

import java.util.function.Consumer;

import com.fmum.client.model.Animator;
import com.fmum.client.module.model.ModelModular;
import com.fmum.common.module.InfoModule;
import com.fmum.common.util.ArmTendency;
import com.fmum.common.util.CoordSystem;
import com.fmum.common.util.Mesh;
import com.fmum.common.util.Vec3;

public class ModelGrip extends ModelModular implements ModelGrable
{
	public final Vec3 grabPos = new Vec3(0D, -5D / 16D, 0D);
	
	public double grabHandRot = 0D;
	
	public double grabArmRot = 0D;
	
	public ModelGrip() { }
	
	public ModelGrip(Mesh... meshes) { super(meshes); }
	
	public ModelGrip(Consumer<ModelGrip> initializer) { initializer.accept(this); }
	
	@Override
	public void updateLeftHandTarPos(
		Animator ani,
		CoordSystem location,
		InfoModule info,
		ArmTendency dst
	) {
		vec.set(this.grabPos);
		info.sys.apply(vec, vec);
		location.apply(vec, vec);
		dst.setHandTarPos(vec);
		dst.setHandTarRotX(this.grabHandRot + ((AnimatorGun)ani).rot.getSmoothedX(1F));
		dst.setArmTarRotX(this.grabArmRot);
	}
	
	@Override
	public void updateRightHandTarPos(
		Animator ani,
		CoordSystem location,
		InfoModule info,
		ArmTendency dst
	) { this.updateLeftHandTarPos(ani, location, info, dst); }
}
