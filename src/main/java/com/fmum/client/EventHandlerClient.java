package com.fmum.client;

import com.fmum.common.FMUM;
import com.fmum.common.item.ItemType;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Collection;

@SideOnly( Side.CLIENT )
@EventBusSubscriber( modid = FMUM.MODID, value = Side.CLIENT )
public final class EventHandlerClient
{
	private EventHandlerClient() { }
	
	@SubscribeEvent
	static void onModelRegister( ModelRegistryEvent evt )
	{
		FMUM.MOD.logInfo( "fmum.on_model_regis" );
		
		final Collection< ItemType > items = ItemType.REGISTRY.values();
		items.forEach( it -> it.onModelRegister( evt ) );
		
		FMUM.MOD.logInfo( "fmum.model_regis_complete", items.size() );
	}
}
