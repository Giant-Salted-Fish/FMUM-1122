package com.fmum.common.module;

import com.fmum.client.FMUMClient;
import com.fmum.common.network.PacketGunOp;
import com.fmum.common.type.ItemPaintable;

import net.minecraft.item.ItemStack;

public interface ItemModular extends ItemPaintable
{
	@Override
	public TypeModular getType();
	
	@Override
	default boolean tagReady(ItemStack stack)
	{
		if(TagModular.validateTag(stack)) return true;
		
		FMUMClient.netHandler.sendToServer(new PacketGunOp(PacketGunOp.INIT_TAG));
		return false;
	}
}
