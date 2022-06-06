package com.fmum.common.module;

import java.util.Set;

import javax.annotation.Nullable;

import com.fmum.common.FMUM;
import com.fmum.common.meta.MetaGrouped;
import com.fmum.common.util.CoordSystem;

import net.minecraft.nbt.NBTTagList;

/**
 * Specifies the properties that a module should have. Things implement this interface can be
 * modified via the customization system provided in {@link FMUM}.
 * 
 * @author Giant_Salted_Fish
 */
public interface MetaModular extends MetaGrouped
{
	@Override
	public default void regisPostInitHandler( Set< Runnable > tasks ) {
		MetaGrouped.super.regisPostInitHandler( tasks );
	}
	
	@Override
	public default void regisPostLoadHandler( Set< Runnable > tasks ) {
		MetaGrouped.super.regisPostLoadHandler( tasks );
	}
	
	@FunctionalInterface
	public static interface ModuleVisitor
	{
		public boolean visit( NBTTagList tag, MetaModular typ, @Nullable CoordSystem sys );
	}
}
