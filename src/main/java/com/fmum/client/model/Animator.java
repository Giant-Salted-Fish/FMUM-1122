package com.fmum.client.model;

import com.fmum.common.util.CoordSystem;
import com.fmum.common.util.Vec3f;

public abstract class Animator
{
	protected static final CoordSystem sys = new CoordSystem();
	
	protected static final Vec3f vec = new Vec3f();
	
	// Camera roll?
	
	public void launchAnimation(Animation animation) { }

	/**
	 * <p>Called in each tick to update animation state.</p>
	 * 
	 * <p>Note that player rotation is updated every frame. Modify it in {@link #renderTick(Model)}
	 * if it is needed. Camera roll should be updated in this method.</p> 
	 * 
	 * @note TODO
	 *     Previous tick rotation of the player can not be trusted. Use {@link #getPrevRotPitch()}
	 *     and {@link #getPrevRotYaw()} if they are needed.
	 */
	public void tick(Model model) { }

	/**
	 * <p>Called right after the rotation of the current player is updated and item holding is yet
	 * about to be rendered.</p>
	 * 
	 * <p>It is recommended to update player rotation in this method. Update camera roll in
	 * {@link #tick(Model)} if it is needed.</p>
	 */
	public void renderTick(Model model) { }
	
	public void fire(Model model) { }
}
