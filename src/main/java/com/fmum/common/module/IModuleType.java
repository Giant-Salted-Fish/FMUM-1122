package com.fmum.common.module;

import com.fmum.common.IDRegistry;
import net.minecraft.nbt.NBTTagCompound;

public interface IModuleType
{
	IDRegistry< IModuleType > REGISTRY = new IDRegistry<>( IModuleType::name );
	
	String name();
	
	IModule createRawModule();
	
	IModule deserializeFrom( NBTTagCompound nbt );
}
