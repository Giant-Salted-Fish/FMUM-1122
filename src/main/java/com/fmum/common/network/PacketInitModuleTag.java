package com.fmum.common.network;

import com.fmum.common.module.ItemModular;
import com.fmum.common.module.TagModular;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;

public final class PacketInitModuleTag implements FMUMPacket
{
	@Override
	public void handleServerSide(EntityPlayerMP player)
	{
		ItemStack stack = player.inventory.getCurrentItem();
		if(!(stack.getItem() instanceof ItemModular))
		{
			// TODO: proper record
			return;
		}
		
		if(!TagModular.validateTag(stack))
			TagModular.setupTag(stack);
	}
}
