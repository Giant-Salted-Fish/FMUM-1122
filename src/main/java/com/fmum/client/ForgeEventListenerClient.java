package com.fmum.client;

import com.fmum.common.FMUM;
import com.fmum.common.ForgeEventListener;
import com.fmum.common.type.ItemInfo;

import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.client.event.RenderSpecificHandEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public abstract class ForgeEventListenerClient extends ForgeEventListener
{
	@SubscribeEvent
	public static void onModelRegister(ModelRegistryEvent evt)
	{
		FMUM.log.info(I18n.format("fmum.onmodelregistration"));
		
		for(RequireItemRegister rir : itemsWaitForRegistration)
			rir.onModelRegister(evt);
		
		FMUM.log.info(
			I18n.format(
				"fmum.modelregistrationcomplete",
				Integer.toString(itemsWaitForRegistration.size())
			)
		);
		itemsWaitForRegistration.clear();
	}
	
	@SubscribeEvent
	public static void onHandRender(RenderSpecificHandEvent evt)
	{
		final ItemStack stack = evt.getItemStack();
		if(stack == null || !(stack.getItem() instanceof ItemInfo)) return;
	}

	@SubscribeEvent
	public static void onHandRender(RenderHandEvent evt)
	{
		
	}
	
//	@SubscribeEvent
//	public static void onPlayerRender(RenderPlayerEvent.Pre evt)
//	{
//		evt.setCanceled(true);
//	}
}
