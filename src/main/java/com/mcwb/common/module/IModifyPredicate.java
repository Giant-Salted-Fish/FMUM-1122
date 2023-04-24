package com.mcwb.common.module;

import com.mcwb.client.MCWBClient;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public interface IModifyPredicate
{
	static final IModifyPredicate OK = new IModifyPredicate() { };
	
	default boolean ok() { return true; }
	
	@SideOnly( Side.CLIENT )
	default boolean okOrNotifyWhy() { return true; }
	
	@FunctionalInterface
	public interface NotOk extends IModifyPredicate
	{
		@Override
		default boolean ok() { return false; }
		
		@Override
		@SideOnly( Side.CLIENT )
		default boolean okOrNotifyWhy()
		{
			MCWBClient.MOD.sendPlayerPrompt( this.why() );
			return false;
		}
		
		@SideOnly( Side.CLIENT )
		String why();
	}
}
