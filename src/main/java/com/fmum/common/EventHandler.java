package com.fmum.common;

import com.fmum.client.player.PlayerPatchClient;
import com.fmum.common.item.IItemType;
import com.fmum.common.network.PacketConfigSync;
import com.fmum.common.player.PlayerPatch;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.PlayerTickEvent;

import java.util.Collection;

@EventBusSubscriber( modid = FMUM.MODID )
final class EventHandler
{
	private EventHandler() { }
	
	static
	{
		final ResourceLocation identifier = new ResourceLocation( FMUM.MODID, "patch" );
		final Object player_patcher = (
			FMUM.MOD.isClient()
			? new Object()
			{
				@SubscribeEvent
				public void onEntityCapAttach( AttachCapabilitiesEvent< Entity > evt )
				{
					final Entity entity = evt.getObject();
					final boolean is_player = entity instanceof EntityPlayer;
					if ( is_player )
					{
						final EntityPlayer player = ( EntityPlayer ) entity;
						final boolean is_player_sp = player instanceof EntityPlayerSP;
						final PlayerPatch player_patch = (
							is_player_sp ? new PlayerPatchClient() : new PlayerPatch() );
						evt.addCapability( identifier, player_patch );
					}
				}
			}
			: new Object()
			{
				@SubscribeEvent
				public void onEntityCapAttach( AttachCapabilitiesEvent< Entity > evt )
				{
					final Entity entity = evt.getObject();
					final boolean is_player = entity instanceof EntityPlayer;
					if ( is_player ) {
						evt.addCapability( identifier, new PlayerPatch() );
					}
				}
			}
		);
		MinecraftForge.EVENT_BUS.register( player_patcher );
	}
	
	@SubscribeEvent
	static void onRegisterItem( RegistryEvent.Register< Item > evt )
	{
		FMUM.MOD.logInfo( "fmum.on_item_regis" );
		
		final Collection< IItemType > items = IItemType.REGISTRY.values();
		items.forEach( it -> it.onItemRegister( evt ) );
		
		FMUM.MOD.logInfo( "fmum.item_regis_complete", items.size() );
	}
	
//	@SubscribeEvent
//	static void onRegisterSound( RegistryEvent.Register< SoundEvent > evt )
//	{
//		FMUM.logInfo( "fmum.on_sound_regis" ); // TODO: translation
//
//		final IForgeRegistry< SoundEvent > registry = evt.getRegistry ();
//		final Collection< SoundEvent > sounds = FMUM.MOD.soundPool.values();
//		sounds.forEach( registry::register );
//
//		FMUM.logInfo( "fmum.sound_regis_complete", sounds.size() );
//
//		// TODO: clear sound pool?
//	}
	
	@SubscribeEvent
	static void onPlayerTick( PlayerTickEvent evt )
	{
		switch ( evt.phase )
		{
		case START: { } break;
		case END:
			final EntityPlayer player = evt.player;
			PlayerPatch.getFrom( player ).tick( player );
			break;
		}
	}
	
	// This event seems to only be posted on server side.
	@SubscribeEvent
	static void onPlayerLogin( PlayerLoggedInEvent evt )
	{
		final EntityPlayerMP player = ( EntityPlayerMP ) evt.player;
		FMUM.MOD.sendPacketS2C( new PacketConfigSync(), player );
	}
}
