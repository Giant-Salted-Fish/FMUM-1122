package com.fmum.common.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.LinkedList;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Helper to read properties/attributes from a local file
 * 
 * @param <T> Destination to save parse result to
 * @author Giant_Salted_Fish
 */
public class LocalAttrParser< T > extends AttrParser< T >
{
	protected final Instantiator< T > instantiator;
	
	/**
	 * @param superParser Parent parser. {@code null} if it is the root parser.
	 */
	public LocalAttrParser( AttrParser< ? super T > superParser ) {
		this( superParser, null );
	}
	
	/**
	 * @param dstClass
	 *     <p> Class of the instance to save parse result to. The given class should has a
	 *     constructor with one parameter of type {@link String}. The name of the source will
	 *     be passed by this parameter. </p>
	 *     
	 *     <p> {@code null} if this parser is not for a leaf data container class. </p>
	 * @param superParser Parent parser. {@code null} if it is a root parser.
	 */
	public LocalAttrParser(
		@Nonnull Class< T > dstClass,
		@Nullable AttrParser< ? super T > superParser
	) {
		super( superParser );
		
		try
		{
			final Constructor< T > constructor = dstClass.getConstructor( String.class );
			
			this.instantiator = name -> {
				try{ return constructor.newInstance( name ); }
				catch(
					InstantiationException
					| IllegalAccessException
					| IllegalArgumentException
					| InvocationTargetException e
				) {
					throw new RuntimeException(
						"Failed to instantiate <" + constructor.getName() + ">",
						e
					);
				}
			};
		}
		catch( NoSuchMethodException | SecurityException e )
		{
			throw new RuntimeException(
				"Failed to get constructor from <" + dstClass.getName() + ">",
				e
			);
		}
	}
	
	public LocalAttrParser(
		@Nullable AttrParser< ? super T > superParser,
		@Nullable Instantiator< T > instantiator
	) {
		super( superParser );
		
		this.instantiator = instantiator;
	}
	
	public T parse( File textFile, String sourceName, Messager sourceTrace )
		throws IOException, Exception
	{
		return this.parse(
			textFile,
			this.instantiator.instantiate( sourceName ),
			sourceTrace
		);
	}
	
	public T parse( BufferedReader textInput, String sourceName, Messager sourceTrace )
		throws IOException, Exception
	{
		return this.parse(
			textInput,
			this.instantiator.instantiate( sourceName ),
			sourceTrace
		);
	}
	
	public T parse( File textFile, T dst, Messager sourceTrace ) throws IOException, Exception
	{
		try( BufferedReader in = new BufferedReader( new FileReader( textFile ) ) ) {
			return this.parse( in, dst, sourceTrace );
		} catch( IOException e ) { throw e; }
	}
	
	public T parse( BufferedReader textInput, T dst, Messager sourceTrace )
		throws IOException, Exception
	{
		// Read all lines from text file
		final LinkedList< String > lines = new LinkedList<>();
		for( String s; ( s = textInput.readLine() ) != null; lines.add( s ) );
		
		// Parse from the lines
		return this.parse( lines, dst, sourceTrace );
	}
	
	@FunctionalInterface
	public static interface Instantiator< T > { public T instantiate( String name ); }
}
