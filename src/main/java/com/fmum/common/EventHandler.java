package com.fmum.common;

import com.fmum.client.player.PlayerPatchClient;
import com.fmum.common.network.PacketConfigSync;
import com.fmum.common.player.PlayerPatch;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.PlayerTickEvent;

@EventBusSubscriber( modid = FMUM.MOD_ID )
final class EventHandler
{
	private EventHandler() { }
	
	static
	{
		final FMUMResource identifier = new FMUMResource( "patch" );
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
	public static void onPlayerTick( PlayerTickEvent evt )
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
	public static void onPlayerLogin( PlayerLoggedInEvent evt )
	{
		final EntityPlayerMP player = ( EntityPlayerMP ) evt.player;
		FMUM.sendPacketTo( new PacketConfigSync(), player );
	}
}
