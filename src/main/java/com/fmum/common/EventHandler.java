package com.fmum.common;

import java.util.Collection;

import com.fmum.common.item.MetaItem;
import com.fmum.common.network.PacketConfigSync;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;

@EventBusSubscriber( modid = FMUM.MODID )
public abstract class EventHandler
{
	private static final AutowireLogger log = new AutowireLogger() { };
	
	private EventHandler() { }
	
	@SubscribeEvent
	public static void onItemRegister( RegistryEvent.Register< Item > evt )
	{
		log.log().info( log.format( "fmum.onitemregistration" ) );
		
		final Collection< MetaItem > values = MetaItem.regis.values();
		for( MetaItem meta : values )
			meta.onItemRegister( evt );
		
		log.log().info(
			log.format(
				"fmum.itemregistrationcomplete",
				Integer.toString( values.size() )
			)
		);
	}
	
	@SubscribeEvent
	public static void onPlayerLogin( PlayerLoggedInEvent evt ) {
		FMUM.net.sendTo( new PacketConfigSync(), ( EntityPlayerMP ) evt.player );
	}
}
