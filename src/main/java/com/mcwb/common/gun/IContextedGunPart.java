package com.mcwb.common.gun;

import com.mcwb.common.modify.IContextedModifiable;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;

import com.mcwb.common.item.IContextedItem;

public interface IContextedGunPart extends IContextedItem, IContextedModifiable
{
	@CapabilityInject( IContextedGunPart.class )
	public static final Capability< IContextedGunPart > CAPABILITY = null;
}
