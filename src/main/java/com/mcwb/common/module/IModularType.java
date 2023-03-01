package com.mcwb.common.module;

import com.mcwb.common.meta.IMeta;
import com.mcwb.common.meta.Registry;

import net.minecraft.nbt.NBTTagCompound;

public interface IModularType extends IMeta
{
	public static final Registry< IModularType > REGISTRY = new Registry<>();
	
//	public IModular< ? > getContexted( ICapabilityProvider provider );
	
	/**
	 * WARNNING: Never use this unless you understand what it is doing. This is only designed to be
	 * used by {@link IModuleSnapshot}
	 */
	public IModular< ? > newPreparedContexted();
	
	/**
	 * WARNNING: Never use this unless you understand what it is doing. This is only designed to be
	 * used by {@link IModular#deserializeNBT(NBTTagCompound)}
	 */
	public IModular< ? > deserializeContexted( NBTTagCompound nbt );
}
