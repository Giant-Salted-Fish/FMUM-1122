package com.fmum;

import com.fmum.network.IPacket;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * @see FMUM#NET
 */
public abstract class WrappedNet
{
	WrappedNet() { }
	
	public abstract void sendPacketS2C( IPacket packet, EntityPlayerMP player );
	
	@SideOnly( Side.CLIENT )
	public abstract void sendPacketC2S( IPacket packet );
}
