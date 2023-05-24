package com.fmum.common;

import java.util.Collection;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import com.fmum.client.player.PlayerPatchClient;
import com.fmum.common.item.IItemType;
import com.fmum.common.network.PacketConfigSync;
import com.fmum.common.player.PlayerPatch;

import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.PlayerTickEvent;
import net.minecraftforge.registries.IForgeRegistry;

@EventBusSubscriber( modid = FMUM.MODID )
final class EventHandler
{
	private static final IAutowireLogger LOGGER = FMUM.MOD;
	
	static
	{
		final BiConsumer< Entity, Consumer< EntityPlayer > > with = ( entity, next ) -> {
			final boolean isPlayer = entity instanceof EntityPlayer;
			if ( isPlayer ) { next.accept( ( EntityPlayer ) entity ); }
		};
		final FMUMResource identifier = new FMUMResource( "patch" );
		final Object eventSubscriber = (
			FMUM.MOD.isClient()
			? new Object() {
				@SubscribeEvent
				public void onEntityCapAttach( AttachCapabilitiesEvent< Entity > evt )
				{
					with.accept( evt.getObject(), player -> {
						final boolean isPlayerSP = player instanceof EntityPlayerSP;
						final PlayerPatch patch = isPlayerSP
							? new PlayerPatchClient( player ) : new PlayerPatch( player );
						evt.addCapability( identifier, patch );
					} );
				}
			}
			: new Object() {
				@SubscribeEvent
				public void onEntityCapAttach( AttachCapabilitiesEvent< Entity > evt )
				{
					with.accept( evt.getObject(), player -> {
						evt.addCapability( identifier, new PlayerPatch( player ) );
					} );
				}
			}
		);
		MinecraftForge.EVENT_BUS.register( eventSubscriber );
	}
	
	private EventHandler() { }
	
	@SubscribeEvent
	public static void onRegisterItem( RegistryEvent.Register< Item > evt )
	{
		LOGGER.logInfo( "fmum.on_item_regis" );
		
		final Collection< IItemType > items = IItemType.REGISTRY.values();
		items.forEach( it -> it.onRegisterItem( evt ) );
		
		LOGGER.logInfo( "fmum.item_regis_complete", items.size() );
	}
	
	@SubscribeEvent
	public static void onRegisterSound( RegistryEvent.Register< SoundEvent > evt )
	{
		LOGGER.logInfo( "fmum.on_sound_regis" ); // TODO: translation
		
		final IForgeRegistry< SoundEvent > registry = evt.getRegistry ();
		final Collection< SoundEvent > sounds = FMUM.MOD.soundPool.values();
		sounds.forEach( registry::register );
		
		LOGGER.logInfo( "fmum.sound_regis_complete", sounds.size() );
		
		// TODO: clear sound pool?
	}
	
	@SubscribeEvent
	public static void onPlayerTick( PlayerTickEvent evt )
	{
		switch ( evt.phase )
		{
		case START: { } break;
		case END: PlayerPatch.get( evt.player ).tick();
		}
	}
	
	// This seems to only be posted on server side.
	@SubscribeEvent
	public static void onPlayerLogin( PlayerLoggedInEvent evt ) {
		FMUM.MOD.sendPacketTo( new PacketConfigSync(), ( EntityPlayerMP ) evt.player );
	}
}
