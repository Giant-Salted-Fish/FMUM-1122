package com.fmum.common.network;

import com.fmum.common.CommonProxy;
import com.fmum.common.module.InfoModule;
import com.fmum.common.module.ItemModular;
import com.fmum.common.module.Slot;
import com.fmum.common.module.TagModular;
import com.fmum.common.module.TypeModular;
import com.fmum.common.util.CoordSystem;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagList;

public final class PacketModuleInstall extends DataModuleLocEx
{
	public PacketModuleInstall() { }
	
	public PacketModuleInstall(byte[] loc, int locLen, int invSlot, int step, int offset) {
		super(loc, locLen, invSlot | offset << 8 | step << 16);
	}
	
	@Override
	public void handleServerSide(EntityPlayerMP player)
	{
		final ItemStack stack = player.inventory.getCurrentItem();
		if(!(stack.getItem() instanceof ItemModular))
		{
			// TODO: Proper log
			return;
		}
		
		if(this.loc.length == 0)
		{
			// Proper log
			return;
		}
		
		final int invSlot = 0xFF & this.assist;
		final int offset = 0xFF & this.assist >>> 8;
		final int step = this.assist >>> 16;
		
		final ItemStack installee = player.inventory.removeStackFromSlot(invSlot);
		if(!(installee.getItem() instanceof ItemModular))
		{
			// Proper log
			return;
		}
		
		// Module to install may never appear in player's hand, hence check if it's tag is ready
		if(!TagModular.validateTag(installee))
			TagModular.setupTag(installee);
		
		// Check max layers
		final TypeModular typeInstallee = ((ItemModular)installee.getItem()).getType();
		final NBTTagList tagInstallee = TagModular.getTag(installee);
		if(
			this.loc.length + (typeInstallee.getDeepestPathNodeCount(tagInstallee) - 1 << 1)
				> CommonProxy.maxLocLen
		) {
			// Proper log
			return;
		}
		
		if(offset >= typeInstallee.offsets.length)
		{
			// Proper log
			return;
		}
		
		// TODO: release base maybe?
		final InfoModule base = InfoModule.get(stack);
		if(base.tryMoveTo(this.loc, this.loc.length - 2) == null)
		{
			// Proper log
			return;
		}
		
		int slotIndex = 0xFF & this.loc[this.loc.length - 2];
		if(slotIndex >= base.type.slots.length)
		{
			// Proper log
			return;
		}
		
		final Slot slot = base.type.slots[slotIndex];
		if(!slot.isAllowed(typeInstallee))
		{
			// Proper log
			return;
		}
		
		if(step > slot.maxStep)
		{
			// Proper log
			return;
		}
		
		final NBTTagList slotTag = (NBTTagList)base.tag.get(1 + slotIndex);
		if(
			slotTag.tagCount() >= CommonProxy.maxCanInstall
			|| slotTag.tagCount() >= slot.maxCanInstall
		) {
			// Proper log
			return;
		}
		
		// Hit box test
		final CoordSystem installPos = CoordSystem.get().set(base.sys);
		installPos.trans(
			slot.x + typeInstallee.getPosX(step, offset, slot.stepLen),
			slot.y,
			slot.z
		);
		installPos.rot(slot.rotX, CoordSystem.Y);
		installPos.submitRot();
		
		boolean conflict = ((ItemModular)stack.getItem()).getType().checkPreviewConflict(
			TagModular.getTag(stack),
			installPos,
			installee
		);
		installPos.release();
		if(conflict)
		{
			// Proper log
			return;
		}
		
		// Test complete! Setup position and install it!
		int[] states = TagModular.getStates(tagInstallee);
		TagModular.setStep(states, step);
		TagModular.setOffset(states, offset);
		slotTag.appendTag(tagInstallee);
	}
}
