package com.fmum.common.network;

import com.fmum.common.gun.ItemGunPart;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;

public class PacketGunOp implements PacketModularOp
{
	public static final byte INIT_TAG = 0;
	
	private int opCode;
	
	public PacketGunOp() { }
	
	public PacketGunOp(byte opCode) { this.opCode = opCode; }
	
	@Override
	public int getOpCode() { return this.opCode; }
	
	@Override
	public void setOpCode(byte opCode) { this.opCode = opCode; }
	
	@Override
	public void handleServerSide(EntityPlayerMP player)
	{
		ItemStack stack = player.inventory.getCurrentItem();
		if(!(stack.getItem() instanceof ItemGunPart))
		{
			// TODO: proper log
			return;
		}
		
		if(
			PacketModularOp.super.doHandleServerSide(player, stack) == EnumActionResult.SUCCESS
//			|| 
		) return;
		
		// proper log
	}
}
