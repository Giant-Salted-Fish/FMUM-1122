package com.fmum.common.network;

import com.fmum.common.module.InfoModule;
import com.fmum.common.module.ItemModular;
import com.fmum.common.module.TagModular;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

public final class PacketModuleRemove extends DataModuleLoc
{
	public PacketModuleRemove() { }
	
	public PacketModuleRemove(byte[] loc, int locLen) { super(loc, locLen); }
	
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
		
		NBTTagList tag = info.tag;
		int slot = 0xFF & this.loc[this.loc.length - 2];
		int index = 0xFF & this.loc[this.loc.length - 1];
		if(info.tryMoveTo(slot, index) == null)
		{
			// Proper log
			return;
		}
		
		tag = (NBTTagList)((NBTTagList)tag.get(1 + slot)).removeTag(index);
		final ItemStack removed = new ItemStack(
			TagModular.getType(tag).item,
			1,
			TagModular.getDam(tag)
		);
		removed.setTagCompound(new NBTTagCompound());
		removed.getTagCompound().setTag(TagModular.TAG, tag);
		
		// If fail to get removed module to player, drop it
		if(!player.addItemStackToInventory(removed))
			player.entityDropItem(removed, 0.5F);
	}
}
