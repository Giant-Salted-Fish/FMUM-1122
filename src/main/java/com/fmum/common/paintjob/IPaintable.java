package com.fmum.common.paintjob;

import net.minecraft.entity.player.EntityPlayer;

import java.util.Optional;

public interface IPaintable
{
	int paintjobCount();
	
	int paintjob();
	
	void setPaintjob( int paintjob_idx );
	
	/**
	 * @return
	 *     Empty if given player can not afford the paintjob. Otherwise, return
	 *     the callback that will actually make given player pay for the deal.
	 */
	Optional< Runnable > testAffordable( EntityPlayer player );
}
