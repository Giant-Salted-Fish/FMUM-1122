package com.mcwb.common.module;

import java.util.function.Function;

import javax.annotation.Nullable;

@FunctionalInterface
public interface IModuleSnapshot
{
	/**
	 * @return {@code null} if required module does not exist
	 */
	@Nullable
	public < T extends IModular< ? > > T setSnapshot( Function< String, T > supplier );
}
