package com.mcwb.common.meta;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;

public interface IContexted
{
	@CapabilityInject( IContexted.class )
	public static final Capability< IContexted > CAPABILITY = null;
}
