package com.fmum.common.type;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.fmum.common.FMUM;
import com.fmum.common.util.Messager;

/**
 * A helper class to read properties of types from plain text. Supported comment symbols are
 * {@code '#'} and {@code "//"}. Suffix commenting is allowed.
 * 
 * @param <T> Typer that this parser services for
 * @author Giant_Salted_Fish
 */
public abstract class TypeTextParser<T> implements ParserFunc<T>
{
	public final HashMap<String, ParserFunc<T>> parsers = new HashMap<>();
	
	protected final TypeTextParser<? super T> superParser;
	
	/**
	 * @param superParser Parent parser. {@code null} if it is the root parser.
	 */
	protected TypeTextParser(@Nullable TypeTextParser<? super T> superParser) {
		this.superParser = superParser;
	}
	
	public T parse(List<String> text, T type, Messager sourceTrace)
	{
		for(String line : text)
		{
			// Skip empty lines and comments
			int i = Math.min(line.indexOf('#'), line.indexOf("//"));
			if((line = i < 0 ? line : line.substring(0, i).trim()).length() > 0)
				this.parse(line.split(" "), type, sourceTrace);
		}
		
		return type;
	}
	
	public ParserFunc<T> addKeyword(String key, ParserFunc<T> parser) {
		return this.parsers.put(key, parser);
	}
	
	public ParserFunc<T> removeKeyParser(String key) { return this.parsers.remove(key); }
	
	@Override
	public void parse(String[] split, T type, Messager sourceTrace)
	{
		ParserFunc<? super T> parser = this.parsers.get(split[0]);
		if(parser == null && (parser = this.superParser) == null)
			FMUM.log.warn(
				FMUM.proxy.format(
					"fmum.unknowntypefilekeyword",
					split[0],
					sourceTrace.message()
				)
			);
		else try { parser.parse(split, type, sourceTrace); }
		catch(Exception e)
		{
			FMUM.log.error(
				FMUM.proxy.format(
					"fmum.errorparsingtypefile",
					split[0],
					sourceTrace.message()
				),
				e
			);
		}
	}
	
	@Override
	public void parse(String[] split, T type) { }
	
	public static final class LocalTypeFileParser<T> extends TypeTextParser<T>
	{
		protected final Constructor<T> constructor;
		
		protected final ParseTargetInstantiator<T> instantiator;
		
		/**
		 * Not for a leaf typer class
		 * 
		 * @param superParser Parent parser. {@code null} if it is the root parser.
		 */
		public LocalTypeFileParser(TypeTextParser<? super T> superParser) {
			this(superParser, null);
		}
		
		/**
		 * @param typeClass
		 *     <p>Class of the type that this parser service for. Constructor of the typer will be
		 *     retrieved from this given class. The given type class should has a constructor with
		 *     one parameter of type {@link String}. The name of the parsing typer will be passed
		 *     via this parameter.</p>
		 *     
		 *     <p>{@code null} if this parser is not for a leaf typer class.</p>
		 * @param superParser Parent parser. {@code null} if it is the root parser.
		 */
		public LocalTypeFileParser(
			@Nonnull Class<T> typeClass,
			@Nullable TypeTextParser<? super T> superParser
		) {
			super(superParser);
			
			try { this.constructor = typeClass.getConstructor(String.class); }
			catch(NoSuchMethodException | SecurityException e)
			{
				throw new RuntimeException(
					FMUM.proxy.format(
						"fmum.failedtogettyperconstructor",
						typeClass.getName()
					),
					e
				);
			}
			
			this.instantiator = name -> {
				try{ return this.constructor.newInstance(name); }
				catch(
					InstantiationException
					| IllegalAccessException
					| IllegalArgumentException
					| InvocationTargetException e
				) {
					throw new RuntimeException(
						FMUM.proxy.format(
							"fmum.failedtoinstantiateitemtyper",
							this.constructor.getName()
						),
						e
					);
				}
			};
		}
		
		/**
		 * @param superParser Parent parser. {@code null} if it is the root parser.
		 * @param instantiator
		 *     Used to instantiate target typer instance while directly read from a local plain text
		 *     file. This allows the caller of this parser to run the parser without having to
		 *     create the typer instance itself. In default called in
		 *     {@link #parse(File, String, String)}. {@code null} if this feature is not needed.
		 */
		public LocalTypeFileParser(
			@Nullable TypeTextParser<? super T> superParser,
			@Nullable ParseTargetInstantiator<T> instantiator
		) {
			super(superParser);
			
			this.constructor = null;
			this.instantiator = instantiator;
		}
		
		public T parse(BufferedReader textInput, T type, Messager sourceTrace) throws IOException
		{
			// Read all lines from text file
			final LinkedList<String> lines = new LinkedList<>();
			for(String s; (s = textInput.readLine()) != null; lines.add(s));
			
			// Parse from the lines
			return this.parse(lines, type, sourceTrace);
		}
		
		public T parse(BufferedReader textInput, String sourceName, Messager sourceTrace)
			throws IOException
		{
			return this.parse(
				textInput,
				this.instantiator.instantiate(sourceName),
				sourceTrace
			);
		}
		
		public T parse(File textFile, T type, Messager sourceTrace) throws IOException
		{
			try(BufferedReader in = new BufferedReader(new FileReader(textFile))) {
				return this.parse(in, type, sourceTrace);
			}
			catch(IOException e) { throw e; }
		}
		
		public T parse(File textFile, String sourceName, Messager sourceTrace) throws IOException
		{
			return this.parse(
				textFile,
				this.instantiator.instantiate(sourceName),
				sourceTrace
			);
		}
	}
	
	public static interface ParseTargetInstantiator<T> {
		public T instantiate(String name);
	}
	
	public static final class KeywordFormatException extends RuntimeException
	{
		private static final long serialVersionUID = 4158902466919166241L;

		public KeywordFormatException(String message) { super(message); }
		
		public KeywordFormatException(String message, Throwable cause) { super(message, cause); }
		
		public static KeywordFormatException keywordArgNotEnough(String atLeast, String supplied)
		{
			return new KeywordFormatException(
				FMUM.proxy.format(
					"fmum.keywordargnumnotenough",
					atLeast,
					supplied
				)
			);
		}
	}
}