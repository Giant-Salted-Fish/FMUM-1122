package com.fmum.common.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.annotation.Nullable;

/**
 * A helper class to read attributes from plain text. {@code '#'} and {@code "//"} comment are
 * supported. Suffix comment is allowed.
 * 
 * @param <T> Destination to save parse result to
 * @author Giant_Salted_Fish
 */
public abstract class AttrParser< T > implements ParserFunc< T >
{
	protected final AttrParser< ? super T > superParser;
	
	protected final HashMap< String, ParserFunc< T > > parsers = new HashMap<>();
	
	/**
	 * @param superParser Parent parser. {@code null} if it is a root parser.
	 */
	public AttrParser( @Nullable AttrParser< ? super T > superParser ) {
		this.superParser = superParser;
	}
	
	/**
	 * Parse the given text
	 * 
	 * @param txt Text to parse
	 * @param dst Destination
	 * @param sourceTrace Used for error trace
	 * @return {@code dst} with attributes set
	 * @throws
	 *     Exception If it is severe enough that could not be handled by
	 *     {@link #handleExcept(Exception, Messager)}
	 */
	public T parse( List< String > txt, T dst, Messager sourceTrace ) throws Exception
	{
		for( String line : txt )
		{
			// Skip empty lines and comments
			int i = Math.min( line.indexOf( '#' ), line.indexOf( "//" ) );
			if( ( line = i < 0 ? line : line.substring( 0, i ).trim() ).length() > 0 )
				try { this.parse( line.split( " " ), dst ); }
				catch( Exception e ) { this.handleExcept( e, sourceTrace ); }
		}
		
		return dst;
	}
	
	@Override
	public void parse( String[] split, T dst )
		throws UnknownKeywordException, KeywordFormatException, Exception
	{
		ParserFunc< ? super T > parser = this.parsers.get( split[ 0 ] );
		if(
			parser == null
			&& ( parser = this.superParser ) == null
			&& ( parser = this.defaultParser() ) == null
		) throw new UnknownKeywordException( split[ 0 ] );
		
		parser.parse( split, dst );
	}
	
	public ParserFunc< T > addKeyword( String key, ParserFunc< T > parser ) {
		return this.parsers.put( key, parser );
	}
	
	public Map< String, ParserFunc< T > > addKeywords( ParserFunc< T > parser, String... keys )
	{
		TreeMap< String, ParserFunc< T > > old = new TreeMap<>();
		for( String k : keys)
		{
			ParserFunc< T > prev = this.parsers.put( k, parser );
			if( prev != null ) old.put( k, prev );
		}
		return old;
	}
	
	public ParserFunc< T > removeKeyword( String key ) { return this.parsers.remove( key ); }
	
	/**
	 * This parser will be used when the parser of the keyword is not found. Throws
	 * {@link UnknownKeywordException} if it is {@code null}.
	 */
	@Nullable
	protected ParserFunc< T > defaultParser() { return null; }
	
	/**
	 * Default exception handler. It simply prints the error and continue the parse.
	 * 
	 * @param e Exception to be handle
	 * @param sourceTrace Provides the source of the text that is parsing
	 * @throws Exception If the exception can not be handled by this {@link AttrParser}
	 */
	protected void handleExcept( Exception e, Messager sourceTrace ) throws Exception
	{
		System.err.println( "An erroc has occurred while parsing <" + sourceTrace + ">" );
		e.printStackTrace();
	}
}