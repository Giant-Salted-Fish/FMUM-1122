package com.mcwb.common.gun;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;

public interface IContextedGun extends IContextedGunPart
{
	@CapabilityInject( IContextedGun.class )
	public static final Capability< IContextedGun > CAPABILITY = null;
}
