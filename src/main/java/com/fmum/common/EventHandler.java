package com.fmum.common;

import java.util.LinkedList;

import net.minecraft.item.Item;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.registries.IForgeRegistry;

@EventBusSubscriber(modid = FMUM.MODID)
public abstract class EventHandler
{
	private EventHandler() { }
	
	public static final LinkedList<RequireItemRegister>
		itemsWaitForRegistration = new LinkedList<>();
	
	@SubscribeEvent
	public static void onItemRegister(RegistryEvent.Register<Item> evt)
	{
		FMUM.log.info(FMUM.proxy.format("fmum.onitemregistration"));
		
		final IForgeRegistry<Item> registry = evt.getRegistry();
		for(RequireItemRegister rir : itemsWaitForRegistration)
			rir.onItemRegister(registry);
		
		FMUM.log.info(
			FMUM.proxy.format(
				"fmum.itemregistrationcomplete",
				Integer.toString(itemsWaitForRegistration.size())
			)
		);
	}
	
	public static interface RequireItemRegister
	{
		public void onItemRegister(IForgeRegistry<Item> registry);
		
		@SideOnly(Side.CLIENT)
		public void onModelRegister(ModelRegistryEvent evt);
	}
}
