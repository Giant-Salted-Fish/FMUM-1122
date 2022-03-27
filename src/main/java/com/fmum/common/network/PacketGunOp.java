package com.fmum.common.network;

import com.fmum.common.gun.ItemGunPart;
import com.fmum.common.gun.TagGun;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;

public class PacketGunOp implements PacketItemOp
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
		
		if(this.handleServerSide(player, stack))
			PacketItemOp.super.handleServerSide(player);
	}
	
	protected boolean handleServerSide(EntityPlayerMP player, ItemStack stack)
	{
		switch(this.opCode)
		{
		case INIT_TAG:
			if(!TagGun.validateTag(stack))
				TagGun.setupTag(stack);
			return false;
		}
		return true;
	}
}
