package com.fmum.client;

import com.fmum.common.FMUM;

import net.minecraft.client.resources.I18n;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ForgeEventListenerClient
{
	@SubscribeEvent
	public static void onModelRegister(ModelRegistryEvent evt)
	{
		FMUM.log.info(I18n.format("fmum.onmodelregistration"));
		
		FMUM.log.info(I18n.format("fmum.modelregistrationcomplete", "0"));
	}
}
