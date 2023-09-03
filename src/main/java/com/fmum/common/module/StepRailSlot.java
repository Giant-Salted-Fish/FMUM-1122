package com.fmum.common.module;

import com.fmum.util.Mat4f;
import com.fmum.util.Vec3f;
import com.google.gson.annotations.SerializedName;

/**
 * Represents slots that allow the adjustment of the installation position by steps along z-axis.
 * Can be used to model typical gun attachment rails, such as picatinny rail and keymode.
 */
public class StepRailSlot extends PointSlot
{
	/**
	 * Rotation of the slot along z-axis.
	 */
	protected float rot_z = 0.0F;
	
	@SerializedName( value = "step_len", alternate = "step_length" )
	protected float step_len = 0.0F;
	
	protected short max_step = 0;
	
	@Override
	public int maxStep() {
		return this.max_step;
	}
	
	@Override
	public void scaleParam( float scale )
	{
		super.scaleParam( scale );
		this.step_len *= scale;
	}
	
	@Override
	public void applyTransform( IModule< ? > child_module, Mat4f dst )
	{
		final Vec3f origin = this.origin;
		final float step_offset = this.step_len * child_module.step();
		dst.translate( origin.x, origin.y, origin.z + step_offset );
		dst.rotateZ( this.rot_z );
	}
}
