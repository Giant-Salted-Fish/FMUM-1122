package com.mcwb.common.module;

public interface IModifyPredicate
{
	public static final IModifyPredicate OK = new IModifyPredicate() { };
	
	public default boolean ok() { return true; }
	
	public default boolean okOrNotifyWhy() { return true; }
	
	@FunctionalInterface
	public static interface NotOk extends IModifyPredicate
	{
		@Override
		public default boolean ok() { return false; }
		
		@Override
		public default boolean okOrNotifyWhy()
		{
			this.notifyWhy();
			return false;
		}
		
		public void notifyWhy();
	}
}
