package com.mcwb.common.modify;

import com.mcwb.common.meta.IHasContext;
import com.mcwb.common.meta.IMeta;
import com.mcwb.common.meta.Registry;
import com.mcwb.common.paintjob.IPaintjob;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

// TODO: it is considered that most of these stuff can be moved into context if it is used without context. Meta itself will act like a simple context getter
public interface IModifiableMeta extends IMeta, IHasContext
{
	public static final Registry< IModifiableMeta > REGISTRY = new Registry<>();
	
	@Override
	public IContextedModifiable getContexted( ICapabilityProvider provider );
	
	/**
	 * @param Usually a clean tag. Created context will bind to it and set required tag to it.
	 * @return A new context with given NBT tag bounden to it
	 */
	public IContextedModifiable newContexted( NBTTagCompound nbt );
	
	/**
	 * Create a new context that bind to nothing.
	 * {@link IContextedModifiable#deserializeNBT(NBTTagCompound)} should be called on the returned
	 * context before actually use it.
	 */
	public IContextedModifiable newRawContexted();
	
	/**
	 * Implement this to accept paintjob injection
	 */
	public void injectPaintjob( IPaintjob paintjob );
}
