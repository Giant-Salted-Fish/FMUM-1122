package com.mcwb.common.module;

import com.mcwb.common.meta.IMeta;
import com.mcwb.common.meta.Registry;

import net.minecraft.nbt.NBTTagCompound;

public interface IModuleType extends IMeta
{
	public static final Registry< IModuleType > REGISTRY = new Registry<>();
	
	/**
	 * WARNNING: Never use this unless you understand what it is doing. This is only designed to be
	 * used by {@link ModuleSnapshot}
	 */
	public IModule< ? > newRawContexted();
	
	/**
	 * WARNNING: Never use this unless you understand what it is doing. This is only designed to be
	 * used by {@link IModular#deserializeNBT(NBTTagCompound)}
	 */
	public IModule< ? > deserializeContexted( NBTTagCompound nbt );
}
