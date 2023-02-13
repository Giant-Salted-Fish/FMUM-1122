package com.mcwb.common.operation;

import com.google.gson.JsonDeserializer;
import com.mcwb.common.MCWB;

public class OperationController implements IOperationController
{
	public static final JsonDeserializer< IOperationController > ADAPTER =
		( json, typeOfT, context ) -> MCWB.GSON.fromJson( json, OperationController.class );
	
	protected static final float[] TIME_ARR = { };
	protected static final String[] EFFECT_ARR = { };
	
	protected float progressor = 0.1F;
	
	protected float[] effectTime = TIME_ARR;
	protected String[] effects = EFFECT_ARR;
	
	protected float[] soundTime = TIME_ARR;
	
	public OperationController() { }
	
	public OperationController( float progress ) { this.progressor = progress; }
	
	public OperationController(
		float progressor,
		float[] effectTime,
		String[] effects,
		float[] soundTime
	) {
		this.progressor = progressor;
		this.effectTime = effectTime;
		this.effects = effects;
		this.soundTime = soundTime;
	}
	
	@Override
	public float progressor() { return this.progressor; }
	
	@Override
	public int effectCount() { return this.effectTime.length; }
	
	@Override
	public float getEffectTime( int idx ) { return this.effectTime[ idx ]; }
	
	@Override
	public String getEffect( int idx ) { return this.effects[ idx ]; }
	
	@Override
	public int soundCount() { return this.soundTime.length; }
	
	@Override
	public float getSoundTime( int idx ) { return this.soundTime[ idx ]; }
}
