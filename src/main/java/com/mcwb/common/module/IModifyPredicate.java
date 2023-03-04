package com.mcwb.common.module;

import javax.annotation.Nullable;

import com.mcwb.client.MCWBClient;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public interface IModifyPredicate
{
	public static final IModifyPredicate NO_PREVIEW = new IModifyPredicate()
	{
		@Override
		public IModular< ? > cursor() { return null; }
	};
	
	public static final IModifyPredicate OK = new IModifyPredicate()
	{
		@Override
		public IModular< ? > cursor() { return null; }
	};
	
	public default boolean ok() { return true; }
	
	@SideOnly( Side.CLIENT )
	public default boolean okOrNotifyWhy() { return true; }
	
	@Nullable
	public IModular< ? > cursor();
	
	@FunctionalInterface
	public static interface NotOk extends IModifyPredicate
	{
		@Override
		public default boolean ok() { return false; }
		
		@Nullable
		@Override
		public default IModular< ? > cursor() { return null; }
		
		@Override
		@SideOnly( Side.CLIENT )
		public default boolean okOrNotifyWhy()
		{
			MCWBClient.MOD.sendPlayerPrompt( this.why() );
			return false;
		}
		
		@SideOnly( Side.CLIENT )
		public String why();
	}
}
