package com.mcwb.common.gun;

import com.mcwb.client.render.IAnimator;
import com.mcwb.util.ArmTracker;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public interface IGun< T extends IGunPart< ? extends T > > extends IGunPart< T >
{
	
	@SideOnly( Side.CLIENT )
	public void setupRenderArm( ArmTracker leftArm, ArmTracker rightArm, IAnimator animator );
}
