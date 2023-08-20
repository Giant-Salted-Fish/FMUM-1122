package com.fmum.common.item;

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
	default void handleItemPacket( ByteBuf buf, EntityPlayer player ) { }
	
	@SideOnly( Side.CLIENT )
	default void handleInputCmd( String cmd, IInputAdapter input ) { }
	
	@SideOnly( Side.CLIENT )
	default boolean shouldHideCrosshair() {
		return false;
	}
}
