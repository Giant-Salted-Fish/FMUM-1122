package com.mcwb.client.module;

import com.mcwb.client.render.IRenderer;
import com.mcwb.util.IReleasable;

@FunctionalInterface
public interface IDeferredRenderer extends IRenderer, IReleasable
{
	@Override
	public default void release() { }
}
