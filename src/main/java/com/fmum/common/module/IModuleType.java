package com.fmum.common.module;

import com.fmum.common.meta.IMeta;
import com.fmum.common.meta.IdRegistry;
import net.minecraft.nbt.NBTTagCompound;

public interface IModuleType extends IMeta
{
	IdRegistry< IModuleType > REGISTRY = new IdRegistry<>();
	
	/**
	 * WARNNING: Never use this unless you understand what it is doing.
	 */
	IModule< ? > newRawContexted();
	
	/**
	 * WARNNING: Never use this unless you understand what it is doing.
	 */
	IModule< ? > deserializeContexted( NBTTagCompound nbt );
}
