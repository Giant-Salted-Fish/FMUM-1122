package com.fmum.gun;

import net.minecraft.entity.player.EntityPlayer;

public interface IGunState
{
	default boolean isBoltCatch() {
		return false;
	}
	
	IGunState charge( EntityPlayer player );
	
	IGunState releaseBolt( EntityPlayer player );
	
	int serializeState();
}
