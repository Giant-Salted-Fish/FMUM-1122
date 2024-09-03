package com.fmum;

import org.apache.logging.log4j.Logger;

/**
 * @see FMUM#LOGGER
 */
public abstract class WrappedLogger
{
	WrappedLogger() { }
	
	public abstract void info( String translate_key, Object... parameters );
	
	public abstract void warn( String translate_key, Object... parameters );
	
	public abstract void error( String translate_key, Object... parameters );
	
	public abstract void exception( Throwable e, String translate_key, Object... parameters );
	
	public abstract Logger unwrap();
}
