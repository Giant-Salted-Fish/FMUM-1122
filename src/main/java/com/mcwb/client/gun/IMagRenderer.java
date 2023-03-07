package com.mcwb.client.gun;

import com.mcwb.common.meta.IContexted;

public interface IMagRenderer< T extends IContexted > extends IGunPartRenderer< T >
{
	/**
	 * Animation applied on mag
	 */
	public static final String CHANNEL_MAG = "mag";
}
