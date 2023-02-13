package com.mcwb.common.operation;

public interface IOperationController
{
	public float progressor();
	
	public int effectCount();
	
	public float getEffectTime( int idx );
	
	public String getEffect( int idx );
	
	public int soundCount();
	
	public float getSoundTime( int idx );
	
	// TODO: play sound
}
