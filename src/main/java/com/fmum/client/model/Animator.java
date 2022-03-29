package com.fmum.client.model;

import com.fmum.client.EventHandlerClient;
import com.fmum.client.FMUMClient;
import com.fmum.common.type.TypeInfo;
import com.fmum.common.util.CoordSystem;
import com.fmum.common.util.Vec3;

import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MouseHelper;

public abstract class Animator
{
	protected static final CoordSystem sys = new CoordSystem();
	
	protected static final Vec3 vec = new Vec3();
	
	public void launchAnimation(Animation animation) { }
	
	/**
	 * <p>Called in each tick to update animation state.</p>
	 * 
	 * <p>Note that player rotation is updated every frame. Modify it in
	 * {@link #itemRenderTick(Model)} if it is needed. Camera roll should be updated in this method.
	 * </p> 
	 * 
	 * @note TODO
	 *     Previous tick rotation of the player can not be trusted. Use {@link #getPrevRotPitch()}
	 *     and {@link #getPrevRotYaw()} if they are needed.
	 */
	public void itemTick(ItemStack stack, TypeInfo type) { }
	
	/**
	 * <p>Called right after the rotation of the current player is updated and item holding is yet
	 * about to be rendered.</p>
	 * 
	 * <p>It is recommended to update player rotation in this method. Update camera roll in
	 * {@link #tick(Model)} if it is needed.</p>
	 */
	public void itemRenderTick(ItemStack stack, TypeInfo type, MouseHelper mouse) { }
	
	public void fire(ItemStack stack, TypeInfo type) { }
	
	/**
	 * Apply position and rotation that indicated by this animator to the given coordinate system.
	 * It can be used to calculate optical effects of sights and other functionalities that related
	 * to the position and pointing of the item.
	 * 
	 * @param sys Copy position and rotation to this system
	 * @param smoother Partial tick time
	 */
	public void applyTransform(CoordSystem sys, float smoother) { }
	
	protected static EntityPlayerSP getPlayer() { return FMUMClient.mc.player; }
	
	protected static float getRenderCamRoll() { return EventHandlerClient.renderCamRoll; }
	
	protected static float getRenderCamYaw() { return EventHandlerClient.renderCamYaw; }
	
	protected static float getRenderCamPitch() { return EventHandlerClient.renderCamPitch; }
	
	protected static void setRenderCamRoll(float camRoll) {
		EventHandlerClient.renderCamRoll = camRoll;
	}
	
	protected static void setRenderCamYaw(float camYaw) {
		EventHandlerClient.renderCamYaw = camYaw;
	}
	
	protected static void setRenderCamPitch(float camPitch) {
		EventHandlerClient.renderCamPitch = camPitch;
	}
	
	protected static float getSmoother() { return Model.smoother; }
}
