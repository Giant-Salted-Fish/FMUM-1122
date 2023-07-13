package com.fmum.common.item;

import com.fmum.client.IInput;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumHand;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public interface IEquippedItem< T extends IItem >
{
	IEquippedItem< ? > VANILLA = new IEquippedItem< IItem >()
	{
		@Override
		public IItem item() { return IItem.VANILLA; }
	};
	
	T item();
	
	default void tickInHand( IItem item, EntityPlayer player, EnumHand hand ) { }
	
	// TODO: Add support for off-hand?
	default void handlePacket( ByteBuf buf, EntityPlayer player ) { }
	
	@SideOnly( Side.CLIENT )
	default void onKeyPress( IInput key ) { }
	
	@SideOnly( Side.CLIENT )
	default void onKeyRelease( IInput key ) { }
	
	@SideOnly( Side.CLIENT )
	default boolean hideCrosshair() { return false; }
}
