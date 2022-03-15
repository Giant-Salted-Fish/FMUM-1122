package com.fmum.common;

import java.util.LinkedList;

import net.minecraft.client.resources.I18n;
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
		final IForgeRegistry<Item> registry = evt.getRegistry();
		for(RequireItemRegistration rir : itemsWaitForRegistration)
			registry.register(rir.getRegistrantItem());
		FMUM.log.info(I18n.format("fmum.itemregistrationcomplete", itemsWaitForRegistration.size()));
		itemsWaitForRegistration.clear();
	}
	
	@FunctionalInterface
	public static interface RequireItemRegistration {
		public Item getRegistrantItem();
	}
}
