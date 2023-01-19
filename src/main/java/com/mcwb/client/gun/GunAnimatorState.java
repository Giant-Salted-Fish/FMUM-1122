package com.mcwb.client.gun;

import com.mcwb.client.item.ItemAnimatorState;
import com.mcwb.util.Mat4f;

public class GunAnimatorState extends ItemAnimatorState
{
	public static final GunAnimatorState INSTANCE = new GunAnimatorState();
	
	public static float dropDistanceCycle = 0F;
	public static float walkDistanceCycle = 0F;
	
	public static float prevPlayerPitch = 0F;
	public static float prevPlayerYaw = 0F;
	
	public final Mat4f m0 = new Mat4f();
}
