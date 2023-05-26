package com.fmum.common.module;

import com.fmum.util.Mat4f;
import com.fmum.util.Vec3f;
import com.google.gson.JsonDeserializer;
import com.google.gson.annotations.SerializedName;

public class RailSlot extends SimpleSlot
{
	public static final JsonDeserializer< RailSlot >
		ADAPTER = ( json, typeOfT, context ) -> context.deserialize( json, RailSlot.class );
	
	/**
	 * Rotation(pointing) of this slot about z-axis.
	 */
	protected float rotZ = 0F;
	
	/**
	 * How far it goes for each adjustment step.
	 */
	@SerializedName( value = "stepLen", alternate = "stepLength" )
	protected float stepLen = 0F;
	
	/**
	 * Max steps that the attachments can go on this slot.
	 */
	protected short maxStep = 0;
	
	@Override
	public int maxStep() { return this.maxStep; }
	
	@Override
	public void scale( float scale )
	{
		this.origin.scale( scale );
		this.stepLen *= scale;
	}
	
	@Override
	public void applyTransform( IModule< ? > installed, Mat4f dst )
	{
		final Vec3f v = this.origin;
		final float step = this.stepLen * installed.step();
		dst.translate( v.x, v.y, v.z + step );
		dst.rotateZ( this.rotZ );
	}
}
