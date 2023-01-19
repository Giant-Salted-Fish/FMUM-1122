package com.mcwb.common.meta;

import net.minecraftforge.common.capabilities.ICapabilityProvider;

@FunctionalInterface
public interface IHasContext {
	public IContexted getContexted( ICapabilityProvider provider );
}
