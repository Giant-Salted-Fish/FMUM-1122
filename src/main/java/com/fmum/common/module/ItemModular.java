package com.fmum.common.module;

import com.fmum.client.FMUMClient;
import com.fmum.common.network.PacketGunOp;
import com.fmum.common.type.ItemPaintable;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public interface ItemModular extends ItemPaintable
{
	@Override
	public TypeModular getType();
	
	@Override
	@SideOnly(Side.CLIENT)
	default boolean tick(ItemStack stack)
	{
		if(TagModular.validateTag(stack))
			return ItemPaintable.super.tick(stack);
		
		FMUMClient.netHandler.sendToServer(new PacketGunOp(PacketGunOp.INIT_TAG));
		return true;
	}
}
