package com.mcwb.common.module;

import com.mcwb.client.MCWBClient;

import net.minecraftforge.fml.relauncher.Side;

import net.minecraftforge.fml.relauncher.SideOnly;

@FunctionalInterface
public interface IPreviewPredicate extends IModifyPredicate
{
	public static final IPreviewPredicate NO_PREVIEW = new IPreviewPredicate()
	{
		@Override
		public int index() { return -1; }
	};
	
	public int index();
	
	@FunctionalInterface
	public static interface NotOk extends IPreviewPredicate
	{
		@Override
		public default boolean ok() { return false; }
		
		@Override
		public default int index() { return -1; }
		
		@Override
		@SideOnly( Side.CLIENT )
		public default boolean okOrNotifyWhy()
		{
			MCWBClient.MOD.sendPlayerMsg( this.why() );
			return false;
		}
		
		@SideOnly( Side.CLIENT )
		public String why();
	}
}
