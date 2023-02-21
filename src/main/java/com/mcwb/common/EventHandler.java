package com.mcwb.common;

import java.util.Collection;

import com.mcwb.client.player.PlayerPatchClient;
import com.mcwb.common.item.IItemType;
import com.mcwb.common.modify.IModifiableType;
import com.mcwb.common.network.PacketConfigSync;
import com.mcwb.common.player.PlayerPatch;

import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.PlayerTickEvent;

@EventBusSubscriber( modid = MCWB.ID )
public final class EventHandler
{
	private static final IAutowireLogger LOGGER = MCWB.MOD;
	
	private EventHandler() { }
	
	@SubscribeEvent
	public static void onItemRegister( RegistryEvent.Register< Item > evt )
	{
		LOGGER.info( "mcwb.on_item_regis" );
		
		final Collection< IItemType > items = IItemType.REGISTRY.values();
		items.forEach( it -> it.onItemRegister( evt ) );
		
		IModifiableType.REGISTRY.values().forEach( IModifiableType::prepareSnapshot );
		
		LOGGER.info( "mcwb.item_regis_complete", items.size() );
	}
	
	@SubscribeEvent
	public static void onEntityCapAttach( AttachCapabilitiesEvent< Entity > evt )
	{
		// Attach logic patch for entity player
		final Entity e = evt.getObject();
		if( !( e instanceof EntityPlayer ) ) return;
		
		// TODO: check if it is ok for other players in the world
		final EntityPlayer player = ( EntityPlayer ) e;
		final boolean client = e.world.isRemote && e instanceof EntityPlayerSP;
		evt.addCapability(
			new MCWBResource( "patch" ),
			client ? new PlayerPatchClient( player ) : new PlayerPatch( player )
		);
	}
	
	@SubscribeEvent
	public static void onPlayerTick( PlayerTickEvent evt )
	{
		switch( evt.phase )
		{
		case START: break;
		case END: PlayerPatch.get( evt.player ).tick();
		}
	}
	
	// This seems to only be posted on server side
	@SubscribeEvent
	public static void onPlayerLogin( PlayerLoggedInEvent evt ) {
		MCWB.MOD.sendTo( new PacketConfigSync(), ( EntityPlayerMP ) evt.player );
	}
}
