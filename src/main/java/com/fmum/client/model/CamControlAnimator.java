package com.fmum.client.model;

import com.fmum.client.EventHandlerClient;
import com.fmum.common.util.CoordSystem;
import com.fmum.common.util.Vec3;

public class CamControlAnimator extends Animator
{
	public static final CamControlAnimator INSTANCE = new CamControlAnimator();
	
	public float
		camRoll = 0F,
		prevCamRoll = 0F;
	
	/**
	 * Position of the shoulder relative to world coordinate system. Arm and item will be rendered
	 * in this coordinate system.
	 */
	public final Vec3 shoulderPos = new Vec3();
	
	/**
	 * Rotation of the shoulder relative to world coordinate system. In default is set as the same
	 * as player's rotation.
	 */
	public final Vec3 shoulderRot = new Vec3();
	
	public float getSmoothedCamRoll(float smoother) {
		return prevCamRoll + (camRoll - prevCamRoll) * smoother;
	}
	
	/**
	 * Updates the actual camera roll
	 */
	@Override
	public void renderTick(Model model)
	{
		// TODO: a shift in shoulder coordinate system
		this.shoulderPos.set(Model.renderEyePosX, Model.renderEyePosY, Model.renderEyePosZ);
		this.shoulderRot.set(0D, Model.renderEyeYaw, Model.renderEyePitch);
		
		EventHandlerClient.actualCameraRoll = this.getSmoothedCamRoll(Model.smoother);
	}
	
	@Override
	public void setupRenderCoordSys(CoordSystem sys, float smoother)
	{
		sys.setDefault();
		sys.globalTrans(this.shoulderPos);
		Vec3 vec = this.shoulderRot;
		sys.globalRot(vec.x, -vec.y - 90F, -vec.z);
	}
}
