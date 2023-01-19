package com.mcwb.common.modify;

import com.google.gson.annotations.SerializedName;
import com.mcwb.util.Mat4f;
import com.mcwb.util.Vec3f;

public class RailSlot extends SimpleSlot
{
	/**
	 * Rotation(pointing) of this slot. Around z-axis
	 */
	@SerializedName( value = "rotZ", alternate = "rotation" )
	protected float rotZ = 0F;
	
	/**
	 * How far it goes for each adjustment step
	 */
	@SerializedName( value = "stepLen", alternate = "stepLength" )
	protected float stepLen = 0F;
	
	/**
	 * Max steps that the attachments can go on this slot
	 */
	protected short maxStep = 0;
	
	@Override
	public int maxStep() { return this.maxStep; }
	
	@Override
	public void scale( float scale )
	{
		this.pos.scale( scale );
		this.stepLen *= scale;
	}
	
	@Override
	public void applyTransform( IContextedModifiable installed, Mat4f dst )
	{
		final Vec3f v = this.pos;
		dst.translate( v.x, v.y, v.z + this.stepLen * installed.step() );
		dst.rotateZ( this.rotZ );
	}
}
