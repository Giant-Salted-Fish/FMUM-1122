package com.fmum.client.model;

import com.fmum.common.type.TypeInfo;

import net.minecraft.item.ItemStack;
import net.minecraft.util.MouseHelper;

public class CamControlAnimator extends Animator
{
	public static final CamControlAnimator INSTANCE = new CamControlAnimator();
	
	public float
		camRoll = 0F,
		prevCamRoll = 0F;
	
	public float getSmoothedCamRoll(float smoother) {
		return prevCamRoll + (camRoll - prevCamRoll) * smoother;
	}
	
	// TODO: camera effects update in itemTick
	
	/**
	 * Updates the actual camera roll
	 */
	@Override
	public void itemRenderTick(ItemStack stack, TypeInfo type, MouseHelper mouse) {
		setRenderCamRoll(this.getSmoothedCamRoll(getSmoother()));
	}
}
