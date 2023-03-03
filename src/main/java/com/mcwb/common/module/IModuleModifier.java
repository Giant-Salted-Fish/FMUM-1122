package com.mcwb.common.module;

@FunctionalInterface
public interface IModuleModifier
{
	public default IModifyPredicate predicate() { return IModifyPredicate.OK; }
	
	/**
	 * @return Cursor
	 */
	public IModular< ? > action();
	
	public default int priority() { return 0; }
	
	@FunctionalInterface
	public static interface NotOk extends IModuleModifier
	{
		@Override
		public IModifyPredicate predicate();
		
		@Override
		public default IModular< ? > action() { return null; }
	}
}
