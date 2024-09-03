package com.fmum.module;

import com.fmum.IDRegistry;
import net.minecraft.nbt.NBTTagCompound;

public interface IModuleType
{
	IDRegistry< IModuleType > REGISTRY = new IDRegistry<>();
	
	
	String getName();
	
	/**
	 * @return A clear module instance with no pre-installed module on it.
	 */
	IModule createRawModule();
	
	/**
	 * @param nbt
	 *     You should not use this NBT tag after calling this method. Copy the
	 *     tag if you do need to use it later.
	 */
	IModule takeAndDeserialize( NBTTagCompound nbt );
}
