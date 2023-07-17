package com.fmum.client;

import com.fmum.common.FMUM;
import com.fmum.common.item.IItemType;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

import java.util.Collection;

//@SideOnly( Side.CLIENT )
@EventBusSubscriber( modid = FMUM.MODID, value = Side.CLIENT )
public final class EventHandlerClient
{
	private EventHandlerClient() { }
	
	@SubscribeEvent
	public static void onModelRegister( ModelRegistryEvent evt )
	{
		FMUM.logInfo( "fmum.on_model_regis" );
		
		final Collection< IItemType > items = IItemType.REGISTRY.values();
		items.forEach( it -> it.onModelRegister( evt ) );
		
		FMUM.logInfo( "fmum.model_regis_complete", items.size() );
	}
}
