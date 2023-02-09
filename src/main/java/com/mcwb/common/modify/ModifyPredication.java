package com.mcwb.common.modify;

public interface ModifyPredication
{
	public static final ModifyPredication NO_PREVIEW = new ModifyPredication() { };
	
	public static final ModifyPredication OK = new ModifyPredication() { };
	
	public default boolean ok() { return true; }
	
	public default boolean okOrNotifyWhy() { return true; }
	
	public default void notifyReason() { }
	
	@FunctionalInterface
	public static interface NotOk extends ModifyPredication
	{
		@Override
		public default boolean ok() { return false; }
		
		@Override
		public default boolean okOrNotifyWhy()
		{
			this.notifyReason();
			return false;
		}
		
		@Override
		public void notifyReason();
	}
}
