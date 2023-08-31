package com.fmum.client.camera;

import com.fmum.util.Mat4f;
import net.minecraft.util.MouseHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly( Side.CLIENT )
public interface ICameraController
{
	void tick();
	
	void prepareRender( MouseHelper mouse );
	
	void getViewMat( Mat4f dst );
}
