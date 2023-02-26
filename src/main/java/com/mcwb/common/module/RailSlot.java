package com.mcwb.common.module;

import com.google.gson.JsonDeserializer;
import com.google.gson.annotations.SerializedName;
import com.mcwb.common.MCWB;
import com.mcwb.util.Mat4f;
import com.mcwb.util.Vec3f;

public class RailSlot extends SimpleSlot
{
	public static final JsonDeserializer< IModuleSlot >
		ADAPTER = ( json, typeOfT, context ) -> MCWB.GSON.fromJson( json, RailSlot.class );
	
	/**
	 * Rotation(pointing) of this slot about z-axis
	 */
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
		this.origin.scale( scale );
		this.stepLen *= scale;
	}
	
	@Override
	public void applyTransform( IModular< ? > module, Mat4f dst )
	{
		final Vec3f v = this.origin;
		dst.translate( v.x, v.y, v.z + this.stepLen * module.step() );
		dst.rotateZ( this.rotZ );
	}
}
