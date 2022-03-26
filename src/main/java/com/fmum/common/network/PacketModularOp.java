package com.fmum.common.network;

public interface PacketModularOp extends PacketItemOp
{
	/**
	 * Base index for operations that do not need to specify location
	 */
	public static final byte NON_MODULE_BASED_OP = 0;
	
	/**
	 * Base index for operations that need specify location
	 */
	public static final byte MODULE_BASED_OP = 16;
	
	/**
	 * Base index for operations that need specify location and extra assist data
	 */
	public static final byte MODULE_BASED_EX = 24;
	
	public static final byte
		MODULE_LASER_TOGGLE = MODULE_BASED_OP + 1,
		MODULE_LIGHT_TOGGLE = MODULE_BASED_OP + 2,
		MODULE_TYPE_TOGGLE = MODULE_BASED_OP + 3,
		UNSTALL_MODULE = MODULE_BASED_OP + 4,
		
		INSTALL_MODULE = MODULE_BASED_EX + 1,
		UPDATE_MODULE_POS = MODULE_BASED_EX + 2,
		UPDATE_PAINTJOB = MODULE_BASED_EX + 3;
	
}
