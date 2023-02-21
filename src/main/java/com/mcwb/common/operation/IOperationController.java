package com.mcwb.common.operation;

import net.minecraft.entity.player.EntityPlayer;

public interface IOperationController
{
	public float progressor();
	
	public int effectCount();
	
	public float getEffectTime( int idx );
	
	public String getEffect( int idx );
	
	public int soundCount();
	
	public float getSoundTime( int idx );
	
	void handlePlaySound( int idx, EntityPlayer player );
}
