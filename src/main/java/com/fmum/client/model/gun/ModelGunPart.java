package com.fmum.client.model.gun;

import java.util.function.Consumer;

import com.fmum.client.model.Animator;
import com.fmum.client.model.module.ModelModular;
import com.fmum.common.module.ModuleInfo;
import com.fmum.common.util.ArmTendency;
import com.fmum.common.util.CoordSystem;
import com.fmum.common.util.Mesh;
import com.fmum.common.util.Vec3;

public class ModelGunPart extends ModelModular
{
	public ModelGunPart() { }
	
	public ModelGunPart(Mesh mesh) { super(mesh); }
	
	public ModelGunPart(Consumer<ModelGunPart> initializer) { initializer.accept(this); }
	
	public static class ModelGrip extends ModelGunPart implements ModelGrable
	{
		public final Vec3 grabPos = new Vec3(0D, -5D / 16D, 0D);
		
		public double grabHandRot = 0D;
		
		public double grabArmRot = 0D;
		
		public ModelGrip() { }
		
		public ModelGrip(Mesh mesh) { super(mesh); }
		
		public ModelGrip(Consumer<ModelGrip> initializer) { initializer.accept(this); }
		
		@Override
		public void updateLeftHandTarPos(
			Animator ani,
			CoordSystem location,
			ModuleInfo info,
			ArmTendency dest
		) {
			vec.set(this.grabPos);
			info.apply(vec, vec);
			location.apply(vec, vec);
			dest.setHandTarPos(vec);
			dest.setHandTarRotX(this.grabHandRot + ((AnimatorGun)ani).rot.getSmoothedX(1F));
			dest.setArmTarRotX(this.grabArmRot);
		}
		
		@Override
		public void updateRightHandTarPos(
			Animator ani,
			CoordSystem location,
			ModuleInfo info,
			ArmTendency dest
		) { this.updateLeftHandTarPos(ani, location, info, dest); }
	}
	
	public static class ModelBolt extends ModelGunPart
	{
		public static final byte
			BOLT = 0,
			CARRIER = 1;
		
		public ModelBolt(Mesh bolt, Mesh carrier)
		{
			this.meshes = new Mesh[] {
				bolt,
				carrier
			};
		}
	}
}
