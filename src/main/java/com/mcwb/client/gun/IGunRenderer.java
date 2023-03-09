package com.mcwb.client.gun;

import com.mcwb.client.operation.IGunOperation;
import com.mcwb.common.meta.IContexted;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public interface IGunRenderer< T extends IContexted > extends IGunPartRenderer< T >
{
	/**
	 * Animation that applied on gun
	 */
	public static final String CHANNEL_GUN = "gun";
	
	/**
	 * Animated left arm orientation in 3D space
	 */
	public static final String CHANNEL_LEFT_ARM = "left_arm";
	
	/**
	 * Animated right arm orientation in 3D space
	 */
	public static final String CHANNEL_RIGHT_ARM = "right_arm";
	
	@SideOnly( Side.CLIENT )
	public void startLoadMag( IGunOperation op );
}
