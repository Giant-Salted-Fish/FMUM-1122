package com.fmum.gunpart;

import gsf.util.animation.TwoBoneIK;
import gsf.util.math.Mat4f;
import gsf.util.math.Vec3f;
import gsf.util.render.IPose;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@FunctionalInterface
@SideOnly( Side.CLIENT )
public interface IHandSetup
{
	IPose get( Vec3f shoulder_pos, float fore_arm_len, float upper_arm_len );
	
	
	static IHandSetup of( IPose pose, Vec3f grip_pos )
	{
		return ( shoulder, fore_len, upper_len ) -> {
			final Vec3f vec = Vec3f.allocate();
			final Vec3f vec1 = Vec3f.allocate();
			vec.set( grip_pos );
			pose.transform( vec, vec );
			vec1.sub( vec, shoulder );
			final TwoBoneIK ik = new TwoBoneIK();
			ik.solve( fore_len, upper_len, vec1, 0.0F );
			Vec3f.release( vec1 );
			
			final Mat4f mat = new Mat4f();
			mat.setIdentity();
			mat.translate( vec );
			mat.eulerRotateYXZ( ik.hand_rot );
			Vec3f.release( vec );
			return IPose.ofMat( mat );
		};
	}
}
