package com.fmum.common;

import java.util.LinkedList;

import net.minecraft.item.Item;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.registries.IForgeRegistry;

public abstract class ForgeEventListener
{
	public static final LinkedList<RequireItemRegistration>
		itemsWaitForRegistration = new LinkedList<>();

	@SubscribeEvent
	public static void onItemRegister(RegistryEvent.Register<Item> evt)
	{
		FMUM.log.info(FMUM.proxy.format("fmum.onitemregistration"));
		final IForgeRegistry<Item> registry = evt.getRegistry();
		for(RequireItemRegistration rir : itemsWaitForRegistration)
			registry.register(rir.getRegistrantItem());
		FMUM.log.info(
			FMUM.proxy.format(
				"fmum.itemregistrationcomplete",
				Integer.toString(itemsWaitForRegistration.size())
			)
		);
		itemsWaitForRegistration.clear();
	}
	
	@FunctionalInterface
	public static interface RequireItemRegistration {
		public Item getRegistrantItem();
	}
}
