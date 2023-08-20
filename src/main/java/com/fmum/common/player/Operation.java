package com.fmum.common.player;

import com.fmum.common.item.IEquippedItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public interface Operation
{
	Operation NONE = new Operation()
	{
		@Override
		public String toString() { return "Operation::NONE"; }
	};
	
	default float progress() { return 1.0F; }
	
	@SideOnly( Side.CLIENT )
	default float smoothedProgress() { return 1.0F; }
	
	default Operation _launch( EntityPlayer player ) { return this; }
	
	default Operation toggle( EntityPlayer player ) { return this; }
	
	default Operation terminate( EntityPlayer player ) { return NONE; }
	
	default Operation tick( EntityPlayer player ) { return this; }
	
	default Operation onOtherTryLaunch(
		Operation other, EntityPlayer player
	) { return this; }
	
	default Operation onEquippedChanged(
		IEquippedItem< ? > new_equipped, EntityPlayer player
	) { return this.terminate( player ); }
}
