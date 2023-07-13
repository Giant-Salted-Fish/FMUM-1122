package com.fmum.common.player;

import com.fmum.common.item.IEquippedItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public interface IOperation
{
	IOperation NONE = new IOperation()
	{
		@Override
		public String toString() { return "Operation::NONE"; }
	};
	
	default float progress() { return 1F; }
	
	@SideOnly( Side.CLIENT )
	default float smoothedProgress() { return 1F; }
	
	default IOperation _launch( EntityPlayer player ) { return this; }
	
	default IOperation toggle( EntityPlayer player ) { return this; }
	
	default IOperation terminate( EntityPlayer player ) { return NONE; }
	
	default IOperation tick( EntityPlayer player ) { return this; }
	
	default IOperation onOtherTryLaunch( IOperation other, EntityPlayer player ) {
		return this;
	}
	
	default IOperation onEquippedChanged( IEquippedItem< ? > new_equipped, EntityPlayer player ) {
		return this.terminate( player );
	}
}
