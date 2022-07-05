package com.fmum.common.meta;

import java.util.Map;

public interface MetaGrouped extends MetaBase
{
	@Override
	public default void regisPostInitHandler( Map< String, Runnable > tasks ) {
		MetaBase.super.regisPostInitHandler( tasks );
	}
	
	@Override
	public default void regisPostLoadHandler( Map< String, Runnable > tasks ) {
		MetaBase.super.regisPostLoadHandler( tasks );
	}
	
	public default String category() { return "default"; }
}
