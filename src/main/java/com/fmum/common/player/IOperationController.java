package com.fmum.common.player;

import com.fmum.common.load.IContentProvider;
import com.fmum.util.IAnimation;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public interface IOperationController
{
	float progressor();
	
	int effectCount();
	
	float getEffectTime( int idx );
	
	String getEffect( int idx );
	
	int soundCount();
	
	float getSoundTime( int idx );
	
	void handlePlaySound( int idx, EntityPlayer player );
	
	@SideOnly( Side.CLIENT )
	IAnimation animation();
	
	@SideOnly( Side.CLIENT )
	void checkAssetsSetup( IContentProvider provider );
}
