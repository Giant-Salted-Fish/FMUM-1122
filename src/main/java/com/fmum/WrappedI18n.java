package com.fmum;

/**
 * @see FMUM#I18N
 */
public abstract class WrappedI18n
{
	WrappedI18n() { }
	
	public abstract String format( String translate_key, Object... parameters );
	
	public abstract boolean hasKey( String translate_key );
}
