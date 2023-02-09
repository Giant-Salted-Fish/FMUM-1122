package com.mcwb.common;

public interface IAutowireSideHandler
{
	public default boolean isClient() { return MCWB.MOD.isClient(); }
	
	public default void clientOnly( Runnable task ) { MCWB.MOD.clientOnly( task ); }
	
//	public < T > T sideOnly( T common, Supplier< ? extends T > client ) { return common; }
}
