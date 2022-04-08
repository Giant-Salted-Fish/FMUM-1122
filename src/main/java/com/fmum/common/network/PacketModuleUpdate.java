package com.fmum.common.network;

import com.fmum.common.module.InfoModule;
import com.fmum.common.module.ItemModular;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;

public final class PacketModuleUpdate extends DataModuleLocEx
{
	public static final byte
		STEP = 0,
		OFFSET = 1,
		PAINT = 2;
	
	public PacketModuleUpdate() { }
	
	public PacketModuleUpdate(byte[] loc, int locLen, int type, int value) {
		super(loc, locLen, type << 16 + value);
	}
	
	@Override
	public void handleServerSide(EntityPlayerMP player)
	{
		final ItemStack stack = player.inventory.getCurrentItem();
		if(!(stack.getItem() instanceof ItemModular))
		{
			// TODO: proper log
			return;
		}
		
		final InfoModule info = new InfoModule(stack);
		if(info.tryMoveTo(this.loc, this.loc.length) == null)
		{
			// Proper log
			return;
		}
		
		int value = 0xFFFF & this.assist;
		switch(this.assist >>> 16)
		{
		case STEP:
			
			break;
		}
	}
}
