package com.mcwb.common.module;

public interface IModuleModifier
{
	public static final IModuleModifier NONE = new IModuleModifier()
	{
		@Override
		public int priority() { return Integer.MIN_VALUE; }
		
		@Override
		public IModifyPredicate action() { return null; }
	};
	
	public IModifyPredicate action();
	
	public int priority();
}
