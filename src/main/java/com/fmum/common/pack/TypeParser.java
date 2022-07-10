package com.fmum.common.pack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.fmum.common.ModWrapper.AutowireLogger;
import com.fmum.common.util.AttrParser;
import com.fmum.common.util.LocalAttrParser;
import com.fmum.common.util.Messager;

/**
 * Extends {@link LocalAttrParser} and print error into {@link AutowireLogger}
 * 
 * @author Giant_Salted_Fish
 */
public class TypeParser< T > extends LocalAttrParser< T > implements AutowireLogger
{
	/**
	 * @see LocalAttrParser#LocalAttrParser(AttrParser)
	 */
	public TypeParser( AttrParser< ? super T > superParser ) { super( superParser ); }
	
	/**
	 * @see LocalAttrParser#LocalAttrParser(Class, AttrParser)
	 */
	public TypeParser(
		@Nonnull Class< T > dstClass,
		@Nullable AttrParser< ? super T > superParser
	) { super( dstClass, superParser ); }
	
	/**
	 * Prints the error with {@link AutowireLogger}
	 */
	@Override
	protected void handleExcept( Exception e, Messager sourceTrace ) throws Exception {
		this.log().error( this.format( "fmum.errorparsingtype", sourceTrace.message() ), e );
	}
}
