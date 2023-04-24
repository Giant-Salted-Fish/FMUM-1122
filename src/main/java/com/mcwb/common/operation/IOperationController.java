package com.mcwb.common.operation;

import net.minecraft.entity.player.EntityPlayer;

public interface IOperationController
{
	float progressor();
	
	int effectCount();
	
	float getEffectTime( int idx );
	
	String getEffect( int idx );
	
	int soundCount();
	
	float getSoundTime( int idx );
	
	void handlePlaySound( int idx, EntityPlayer player );
}
