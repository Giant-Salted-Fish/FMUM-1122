package com.fmum.common.module;

import com.fmum.client.FMUMClient;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public interface IModifyPredicate
{
	IModifyPredicate OK = new IModifyPredicate() { };
	
	default boolean ok() { return true; }
	
	@SideOnly( Side.CLIENT )
	default boolean okOrNotifyWhy() { return true; }
	
	@FunctionalInterface
	interface NotOk extends IModifyPredicate
	{
		@Override
		default boolean ok() { return false; }
		
		@Override
		@SideOnly( Side.CLIENT )
		default boolean okOrNotifyWhy()
		{
			FMUMClient.sendPlayerPrompt( this.why() );
			return false;
		}
		
		@SideOnly( Side.CLIENT )
		String why();
	}
}
