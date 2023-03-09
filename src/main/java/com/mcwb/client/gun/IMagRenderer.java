package com.mcwb.client.gun;

import com.mcwb.common.meta.IContexted;

public interface IMagRenderer< T extends IContexted > extends IGunPartRenderer< T >
{
	/**
	 * Animation applied on mag
	 */
	public static final String CHANNEL_MAG = "mag";
	
	/**
	 * Animation applied on the mag that is going to be loaded into gun
	 */
	public static final String CHANNEL_LOADING_MAG = "loading_mag";
}
