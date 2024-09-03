package com.fmum.player;

import gsf.util.animation.IPoseSetup;
import net.minecraft.util.MouseHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly( Side.CLIENT )
public interface IPlayerCamera
{
	String ANIM_CHANNEL = "camera";
	
	
	void tickCamera();
	
	void prepareRender( MouseHelper mouse );
	
	IPoseSetup getCameraSetup();
}
