package com.fmum.common.item;

import com.fmum.client.input.Input;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumHand;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public interface EquippedItem< T extends Item >
{
	EquippedItem< ? > VANILLA = new EquippedItem< Item >()
	{
		@Override
		public Item item() { return Item.VANILLA; }
	};
	
	T item();
	
	default void tickInHand( Item item, EntityPlayer player, EnumHand hand ) { }
	
	// TODO: Add support for off-hand?
	default void onItemPacket( ByteBuf buf, EntityPlayer player ) { }
	
	@SideOnly( Side.CLIENT )
	default void onInputSignal( String signal, Input input ) { }
	
	@SideOnly( Side.CLIENT )
	default boolean shouldDisableCrosshair() {
		return false;
	}
}
