package com.mcwb.common.modify;

import com.mcwb.common.meta.IMeta;
import com.mcwb.common.meta.Registry;
import com.mcwb.common.paintjob.IPaintjob;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

public interface IModifiableType extends IMeta
{
	public static final Registry< IModifiableType > REGISTRY = new Registry<>();
	
	public IModifiable getContexted( ICapabilityProvider provider );
	
	public IModifiable newContexted( NBTTagCompound nbt );
	
	public IModifiable deserializeContexted( NBTTagCompound nbt );
	
	/**
	 * Implement this to accept paintjob injection
	 */
	public void injectPaintjob( IPaintjob paintjob );
}
