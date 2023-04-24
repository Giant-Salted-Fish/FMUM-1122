package com.mcwb.common.gun;

import com.mcwb.client.render.IAnimator;
import com.mcwb.common.item.IEquippedItem;
import com.mcwb.util.ArmTracker;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public interface IEquippedGun< T extends IGun< ? > > extends IEquippedItem< T >
{
//	@SideOnly( Side.CLIENT )
//	float getAimProgress( float smoother );
	
	// TODO: equipped itself actually has animator. may be just use it
	@SideOnly( Side.CLIENT )
	void setupRenderArm( IAnimator animator, ArmTracker leftArm, ArmTracker rightArm );
}
