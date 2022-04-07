package com.fmum.common.network;

import com.fmum.common.CommonProxy;
import com.fmum.common.module.InfoModule;
import com.fmum.common.module.ItemModular;
import com.fmum.common.module.TagModular;
import com.fmum.common.module.TypeModular;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;

public final class PacketInstallModule extends DataModuleLocEx
{
	public PacketInstallModule() { }
	
	public PacketInstallModule(byte[] loc, int locLen, int invSlot) { super(loc, locLen, invSlot); }
	
	@Override
	public void handleServerSide(EntityPlayerMP player)
	{
		ItemStack stack = player.inventory.getCurrentItem();
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
		
		ItemStack installee = player.inventory.getStackInSlot(this.assist);
		if(!(installee.getItem() instanceof ItemModular))
		{
			// Proper log
			return;
		}
		
		// Module to install may never appear in player's hand, hence check if it's tag is ready
		if(!TagModular.validateTag(installee))
			TagModular.setupTag(installee);
		
		// Check max layers
		TypeModular typeInstallee = ((ItemModular)installee.getItem()).getType();
		if(
			this.loc.length + (typeInstallee.getNodeDepth(TagModular.getTag(installee)) - 1 << 1)
				> CommonProxy.maxLocLen
		) {
			// Proper log
			return;
		}
		
		InfoModule info = new InfoModule(stack);
		if(info.tryMoveTo(this.loc, this.loc.length - 2) == null)
		{
			// Proper log
			return;
		}
		
		
	}
}
