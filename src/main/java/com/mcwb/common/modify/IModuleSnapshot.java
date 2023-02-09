package com.mcwb.common.modify;

import java.util.function.Function;

import javax.annotation.Nullable;

@FunctionalInterface
public interface IModuleSnapshot
{
	/**
	 * @return {@code null} if required module does not exist
	 */
	@Nullable
	public < T extends IModifiable > T initContexted( Function< String, T > getter );
}
