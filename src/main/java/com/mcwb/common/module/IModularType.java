package com.mcwb.common.module;

import com.mcwb.common.meta.IMeta;
import com.mcwb.common.meta.Registry;

import net.minecraft.nbt.NBTTagCompound;

public interface IModularType extends IMeta
{
	public static final Registry< IModularType > REGISTRY = new Registry<>();
	
	public void compileSnapshot();
	
//	public IModular< ? > getContexted( ICapabilityProvider provider );
	
	/**
	 * @return A fresh contexted without wrapper
	 */
	public IModular< ? > newContexted();
	
	/**
	 * @return A contexted deserialized from given nbt without wrapper
	 */
	public IModular< ? > deserializeContexted( NBTTagCompound nbt );
}
