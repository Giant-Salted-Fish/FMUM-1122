package com.fmum.common.meta;

import java.util.Set;

public interface MetaGrouped extends MetaBase
{
	@Override
	public default void regisPostInitHandler( Set< Runnable > tasks ) {
		MetaBase.super.regisPostInitHandler( tasks );
	}
	
	@Override
	public default void regisPostLoadHandler( Set< Runnable > tasks ) {
		MetaBase.super.regisPostLoadHandler( tasks );
	}
	
	public default String category() { return "default"; }
}
