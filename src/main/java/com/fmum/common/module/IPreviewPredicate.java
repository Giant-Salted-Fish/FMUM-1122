package com.fmum.common.module;

import com.fmum.client.FMUMClient;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@FunctionalInterface
public interface IPreviewPredicate extends IModifyPredicate
{
	IPreviewPredicate NO_PREVIEW = () -> -1;
	
	int index();
	
	@FunctionalInterface
	interface NotOk extends IPreviewPredicate
	{
		@Override
		default boolean ok() { return false; }
		
		@Override
		default int index() { return -1; }
		
		@Override
		@SideOnly( Side.CLIENT )
		default boolean okOrNotifyWhy()
		{
			FMUMClient.MOD.sendPlayerMsg( this.why() );
			return false;
		}
		
		@SideOnly( Side.CLIENT )
		String why();
	}
}
