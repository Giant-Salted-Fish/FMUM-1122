package com.mcwb.client.gun;

import com.mcwb.util.ArmTracker;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly( Side.CLIENT )
public class GunAnimatorState extends GunPartAnimatorState
{
	public static final GunAnimatorState INSTANCE = new GunAnimatorState();
	
	public static float dropDistanceCycle = 0F;
	public static float walkDistanceCycle = 0F;
	
	public static float prevPlayerPitch = 0F;
	public static float prevPlayerYaw = 0F;
	
	public final ArmTracker leftArm = new ArmTracker();
	public final ArmTracker rightArm = new ArmTracker();
	
	public GunAnimatorState()
	{
		this.leftArm.shoulderPos.set( 5F / 16F, -4F / 16F, 0F );
		this.rightArm.shoulderPos.set( -3F / 16F, -4F / 16F, -2.5F / 16F );
	}
}
