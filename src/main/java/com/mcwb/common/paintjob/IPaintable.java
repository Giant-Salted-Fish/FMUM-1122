package com.mcwb.common.paintjob;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public interface IPaintable
{
	int paintjobCount();
	
	int paintjob();
	
	void setPaintjob( int paintjob );
	
	boolean tryOffer( int paintjob, EntityPlayer player );
	
	@SideOnly( Side.CLIENT )
	boolean tryOfferOrNotifyWhy( int paintjob, EntityPlayer player );
}
