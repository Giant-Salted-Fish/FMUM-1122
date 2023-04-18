package com.mcwb.common.module;

import com.mcwb.common.meta.IMeta;
import com.mcwb.common.meta.Registry;

import net.minecraft.nbt.NBTTagCompound;

public interface IModuleType extends IMeta
{
	public static final Registry< IModuleType > REGISTRY = new Registry<>();
	
	/**
	 * WARNNING: Never use this unless you understand what it is doing.
	 */
	public IModule< ? > newRawContexted();
	
	/**
	 * WARNNING: Never use this unless you understand what it is doing.
	 */
	public IModule< ? > deserializeContexted( NBTTagCompound nbt );
}
