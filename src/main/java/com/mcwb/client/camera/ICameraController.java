package com.mcwb.client.camera;

import com.mcwb.client.input.Key;
import com.mcwb.util.Vec3f;

import net.minecraft.util.MouseHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Interface for instances that controls the camera orientation for rendering. The default
 * implementations of this interface should provide the ability to hold {@link Key#FREE_VIEW}
 * to freely rotate your head and be able to adjust the orientation for camera animation.
 * 
 * @author Giant_Salted_Fish
 */
@SideOnly( Side.CLIENT )
public interface ICameraController
{
	public void tick();
	
	public void prepareRender( MouseHelper mouse );
	
	/**
	 * @param dst Will save camera orientation into this vector.
	 */
	public void getCameraRot( Vec3f dst );
	
	public void getPlayerRot( Vec3f dst );
}
