package com.fmum.common;

import java.util.LinkedList;

import net.minecraft.item.Item;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.registries.IForgeRegistry;

public final class ForgeEventListener
{
	public static final LinkedList<RequireItemRegistration>
		itemsWaitForRegistration = new LinkedList<>();
	
	@SubscribeEvent
	public void onItemRegister(RegistryEvent.Register<Item> evt)
	{
		for(
			int i = itemsWaitForRegistration.size();
			--i >= 0;
			itemsWaitForRegistration.poll().onRegister(evt.getRegistry())
		);
	}
	
	@FunctionalInterface
	public static interface RequireItemRegistration {
		public void onRegister(IForgeRegistry<Item> registry);
	}
}
