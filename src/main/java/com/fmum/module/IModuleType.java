package com.fmum.module;

import com.fmum.BiRegistry;
import com.fmum.load.JsonData;
import net.minecraft.nbt.NBTTagCompound;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

public interface IModuleType
{
	BiRegistry< Short, IModuleType > REGISTRY = BiRegistry.createWithShortKey();
	
	
	String getName();
	
	/**
	 * @param data Setup data for this module.
	 * @param lookup Function to look up module type via name.
	 * @return A factory to create this module with give setup applied.
	 */
	Supplier< ? extends IModule > buildSetupFactory(
		JsonData data,
		Function< String, Optional< ? extends IModuleType > > lookup
	);
	
	/**
	 * @param nbt
	 *     You should not use this NBT tag after calling this method. Copy the
	 *     tag if you do need to use it later.
	 */
	IModule takeAndDeserialize( NBTTagCompound nbt );
}
