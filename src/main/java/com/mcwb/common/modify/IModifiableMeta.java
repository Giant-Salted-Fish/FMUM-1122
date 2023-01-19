package com.mcwb.common.modify;

import com.mcwb.common.meta.ICategoried;
import com.mcwb.common.meta.IHasContext;
import com.mcwb.common.meta.IMeta;
import com.mcwb.common.meta.Registry;
import com.mcwb.common.paintjob.IPaintjob;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

public interface IModifiableMeta extends IMeta, IHasContext, ICategoried
{
	public static final Registry< IModifiableMeta > REGISTRY = new Registry<>();
	
	/**
	 * Modifiable should always make sure that nbt is not {@code null}
	 */
	@Override
	public IContextedModifiable getContexted( ICapabilityProvider provider );
	
	public IContextedModifiable newContexted( NBTTagCompound nbt );
	
	public IModuleSlot getSlot( int idx );
	
	public int slotCount();
	
	public int offsetCount();
	
	public int paintjobCount();
	
	/**
	 * Implement this to accept paintjob injection
	 */
	public void injectPaintjob( IPaintjob paintjob );
}
