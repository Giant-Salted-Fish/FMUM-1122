package com.fmum.client.model;

import com.fmum.common.util.CoordSystem;
import com.fmum.common.util.Vec3;

public abstract class Animator
{
	protected static final CoordSystem sys = new CoordSystem();
	
	protected static final Vec3 vec = new Vec3();
	
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
	
	/**
	 * Setup the coordinate system that this item should located in when rendering. In default it
	 * just load identity for the given system.
	 * 
	 * @param sys
	 *     Coordinate system to load render coordinate system into. Note that the input system could
	 *     be a raw system so it is recommended to call {@link CoordSystem#setDefault()} before
	 *     apply any transform.
	 */
	public void setupRenderCoordSys(CoordSystem sys, float smoother) { sys.setDefault(); }
	
	/**
	 * Apply position and rotation that indicated by this animator to the given coordinate system.
	 * It can be used to calculate optical effects of sights and other functionalities that related
	 * to the position and pointing of the item.
	 * 
	 * @param sys Copy position and rotation to this system
	 * @param smoother Partial tick time
	 */
	public void apply(CoordSystem sys, float smoother) { }
}
