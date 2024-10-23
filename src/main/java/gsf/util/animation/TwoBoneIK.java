package gsf.util.animation;

import gsf.util.math.Quat4f;
import gsf.util.math.Vec3f;
import net.minecraft.util.math.MathHelper;

public class TwoBoneIK
{
	public final Vec3f elbow_pos = new Vec3f();
	public final Vec3f hand_rot = new Vec3f();
	
	public void solve( float fore_arm_len, float upper_arm_len, Vec3f hand_pos, float arm_rot_z )
	{
		final Vec3f vec = Vec3f.allocate();
		
		// Get distance from hand to shoulder.
		final float dist_q2 = hand_pos.lengthSquared();
		final float dist = MathHelper.sqrt( dist_q2 );
		
		// Case: Distance is too short to form a triangle.
		if ( fore_arm_len >= upper_arm_len + dist || upper_arm_len >= fore_arm_len + dist )
		{
			if ( vec.isOrigin() )
			{
				// If the hand is at the shoulder, then the arm is straight downward.
				this.elbow_pos.set( 0.0F, -upper_arm_len, 0.0F );
			}
			else
			{
				this.elbow_pos.set( hand_pos );
				if ( fore_arm_len > upper_arm_len ) {
					this.elbow_pos.negate();
				}
				this.elbow_pos.scale( upper_arm_len / dist );
			}
		}
		// Case: Distance is too long and the arm cannot reach it.
		else if ( dist >= fore_arm_len + upper_arm_len )
		{
			this.elbow_pos.set( hand_pos );
			this.elbow_pos.scale( upper_arm_len / dist );
		}
		// Case: Normal triangle shape.
		else
		{
			final float a = fore_arm_len;
			final float b = dist;
			final float a_p2 = a * a;
			final float b_p2 = dist_q2;
			final float c_p2 = upper_arm_len * upper_arm_len;
			final float cos = ( a_p2 + b_p2 - c_p2 ) / ( 2.0F * a * b );
			final float sin = MathHelper.sqrt( 1.0F - cos * cos );
			this.elbow_pos.set( 0.0F, -fore_arm_len * sin, dist - fore_arm_len * cos );
			
			// Get elbow coordinate in 3D space.
			hand_pos.getEulerAngle( vec );
			final Quat4f quat = Quat4f.allocate();
			quat.setRotY( vec.y );
			quat.rotateX( vec.x );
			quat.rotateZ( arm_rot_z );
			quat.transform( this.elbow_pos, this.elbow_pos );
			Quat4f.release( quat );
		}
		
		vec.set( hand_pos );
		vec.sub( this.elbow_pos );
		vec.getEulerAngle( this.hand_rot );
		Vec3f.release( vec );
	}
}
