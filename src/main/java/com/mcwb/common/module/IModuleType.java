package com.mcwb.common.module;

import com.mcwb.common.meta.IMeta;
import com.mcwb.common.meta.Registry;

import net.minecraft.nbt.NBTTagCompound;

public interface IModuleType extends IMeta
{
	static final Registry< IModuleType > REGISTRY = new Registry<>();
	
	/**
	 * WARNNING: Never use this unless you understand what it is doing.
	 */
	IModule< ? > newRawContexted();
	
	/**
	 * WARNNING: Never use this unless you understand what it is doing.
	 */
	IModule< ? > deserializeContexted( NBTTagCompound nbt );
}
