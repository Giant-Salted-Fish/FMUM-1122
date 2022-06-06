package com.fmum.common.util;

import java.util.HashMap;
import java.util.List;

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
	public final HashMap< String, ParserFunc< T > > parsers = new HashMap<>();
	
	protected final AttrParser< ? super T > superParser;
	
	/**
	 * @param superParser Parent parser. {@code null} if it is a root parser.
	 */
	protected AttrParser( @Nullable AttrParser< ? super T > superParser ) {
		this.superParser = superParser;
	}
	
	public T parse( List< String > text, T dst, Messager sourceTrace )
	{
		for( String line : text )
		{
			// Skip empty lines and comments
			int i = Math.min( line.indexOf( '#' ), line.indexOf( "//" ) );
			if( ( line = i < 0 ? line : line.substring( 0, i ).trim() ).length() > 0 )
				this.parse( line.split( " " ), dst, sourceTrace );
		}
		
		return dst;
	}
	
	public ParserFunc< T > addKeyword( String key, ParserFunc< T > parser ) {
		return this.parsers.put( key, parser );
	}
	
	public ParserFunc< T > removeKeyParser( String key ) { return this.parsers.remove( key ); }
	
	/**
	 * This parser will be used when the parser of the keyword is not found. Throws
	 * {@link UnknownKeywordException} if it is {@code null}.
	 */
	@Nullable
	public ParserFunc< T > defaultParser() { return null; }
	
	@Override
	public void parse( String[] split, T dst, Messager sourceTrace )
		throws UnknownKeywordException, KeywordFormatException
	{
		ParserFunc< ? super T > parser = this.parsers.get( split[ 0 ] );
		if(
			parser == null
			&& ( parser = this.superParser ) == null
			&& ( parser = this.defaultParser() ) == null
		) throw new UnknownKeywordException( split[ 0 ], sourceTrace.message() );
		else try { parser.parse( split, dst, sourceTrace ); }
		catch( Exception e ) {
			throw new KeywordFormatException( split[ 0 ], sourceTrace.message(), e );
		}
	}
	
	@Override
	public void parse( String[] split, T dst ) { }
}