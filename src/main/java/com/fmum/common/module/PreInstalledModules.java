package com.fmum.common.module;

import javax.annotation.Nullable;

import net.minecraft.nbt.NBTTagList;

/**
 * Describe the modules that has been pre-installed on the other modules
 * 
 * @author Giant_Salted_Fish
 */
public interface PreInstalledModules
{
	public void writeToTag( NBTTagList tag );
	
	/**
	 * @return Tag of the corresponding module. {@code null} failed to do so.
	 */
	@Nullable
	public NBTTagList genTag();
}
