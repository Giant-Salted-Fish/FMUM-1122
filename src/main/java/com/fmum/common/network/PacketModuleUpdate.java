package com.fmum.common.network;

import com.fmum.common.module.InfoModule;
import com.fmum.common.module.ItemModular;
import com.fmum.common.module.TagModular;
import com.fmum.common.module.TypeModular;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;

public final class PacketModuleUpdate extends DataModuleLocEx
{
	public static final byte
		STEP = 0,
		OFFSET = 1,
		PAINT = 2;
	
	public PacketModuleUpdate() { }
	
	/**
	 * @param op One of {@link #STEP}, {@link #OFFSET} and {@link #PAINT}
	 */
	public PacketModuleUpdate(byte[] loc, int locLen, int op, int value) {
		super(loc, locLen, op << 16 | value);
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
		
		if(!TagModular.validateTag(stack))
			TagModular.setupTag(stack);
		
		int op = this.assist >>> 16;
		int value = 0xFFFF & this.assist;
		switch(op)
		{
		case STEP:
		{
			if(this.loc.length == 0)
			{
				// Proper log
				return;
			}
			
			// TODO: release info maybe?
			final InfoModule info = InfoModule.get(stack);
			if(info.tryMoveTo(this.loc, this.loc.length - 2) == null)
			{
				// Proper log
				return;
			}
			
			final TypeModular baseType = info.type;
			int slot = 0xFF & this.loc[this.loc.length - 2];
			if(info.tryMoveTo(slot, 0xFF & this.loc[this.loc.length - 1]) == null)
			{
				// Proper log
				return;
			}
			
			if(value > baseType.slots[slot].maxStep)
			{
				// Proper log
				return;
			}
			
			TagModular.setStep(TagModular.getStates(info.tag), value);
			break;
		}
		case OFFSET:
		case PAINT:
			// TODO: release info maybe?
			final InfoModule info = InfoModule.get(stack);
			if(info.tryMoveTo(this.loc, this.loc.length) == null)
			{
				// Proper log
				return;
			}
			
			if(op == OFFSET)
			{
				if(value < info.type.offsets.length)
					TagModular.setOffset(TagModular.getStates(info.tag), value);
				else ;// Proper log
			}
			else if(value < info.type.paintjobs.size())
			{
				// TODO: remove materials required from player's inventory
				TagModular.setDam(TagModular.getStates(info.tag), value);
			}
			else ;// Proper log
		}
	}
}
