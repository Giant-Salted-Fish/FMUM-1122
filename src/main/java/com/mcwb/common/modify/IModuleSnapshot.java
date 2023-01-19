package com.mcwb.common.modify;

import java.util.function.BiFunction;

import javax.annotation.Nullable;

import net.minecraft.nbt.NBTTagCompound;

@FunctionalInterface
public interface IModuleSnapshot
{
	/**
	 * @return {@code null} if required module does not exist
	 */
	@Nullable
	public < T extends IContextedModifiable > T initContexted(
		BiFunction< String, NBTTagCompound, T > getter
	);
}
