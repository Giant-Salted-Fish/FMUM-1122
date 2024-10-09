package com.fmum.player;

import gsf.util.render.IPose;
import net.minecraft.util.MouseHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly( Side.CLIENT )
public interface IPlayerCamera
{
	String CHANNEL_CAMERA = "camera";
	
	
	void tickCamera();
	
	void prepareRender( MouseHelper mouse );
	
	IPose getCameraSetup();
}
