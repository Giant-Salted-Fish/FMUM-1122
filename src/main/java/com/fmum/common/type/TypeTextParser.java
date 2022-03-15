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

import com.fmum.common.FMUM;
import com.fmum.common.pack.LocalContentProvider;

import net.minecraft.client.resources.I18n;

/**
 * A helper class to read properties of types from plain text
 * 
 * @param <T> Typer that this parser services for
 * @author Giant_Salted_Fish
 */
public abstract class TypeTextParser<T extends TypeInfo> implements ParserFunc<T>
{
	public final HashMap<String, ParserFunc<T>> parsers = new HashMap<>();
	
	protected final TypeTextParser<? super T> superPaser;
	
	protected final Constructor<T> instantiator;
	
	/**
	 * @param superPaser Parent parser. {@code null} if it is the root parser.
	 * @param typeClass
	 *     Class of the type that this parser service for. Constructor of the typer will be
	 *     retrieved from this given class. The given type class should has a constructor with two
	 *     parameters of type {@link String}. The name of the parsing typer will be passed via first
	 *     parameter. The name of the source(usually is the content pack name) will be provided via
	 *     second parameter.
	 */
	protected TypeTextParser(TypeTextParser<? super T> superPaser, Class<T> typeClass)
	{
		this.superPaser = superPaser;
		try { this.instantiator = typeClass.getConstructor(String.class, String.class); }
		catch(NoSuchMethodException | SecurityException e)
		{
			throw new RuntimeException(
				I18n.format(
					"fmum.failedtogettyperconstructor",
					typeClass.getName()
				),
				e
			);
		}
	}
	
	public T parse(List<String> text, T type, String sourceTrace)
	{
		for(String line : text)
		{
			// Skip empty lines and comments
			int i = line.indexOf("//");
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
	public void parse(String[] split, T type, String sourceTrace)
	{
		ParserFunc<? super T> parser = this.parsers.get(split[0]);
		if(parser == null && (parser = this.superPaser) == null)
			FMUM.log.warn(I18n.format("fmum.unknowntypefilekeyword", split[0], sourceTrace));
		else try { parser.parse(split, type); }
		catch(Exception e) {
			FMUM.log.error(I18n.format("fmum.errorparsingtypefile", split[0], sourceTrace), e);
		}
	}
	
	@Override
	public void parse(String[] split, T type) { }
	
	public static final class LocalTypeFileParser<T extends TypeInfo> extends TypeTextParser<T>
	{
		public LocalTypeFileParser(TypeTextParser<? super T> superPaser, Class<T> typeClass) {
			super(superPaser, typeClass);
		}
		
		public T parse(File textFile, String packName, String typePath)
		{
			final String fName = textFile.getName();
			final String sourceTraceName = packName + "." + typePath + "." + fName;
			
			// Read all lines from text file
			final LinkedList<String> lines = new LinkedList<>();
			try(BufferedReader in = new BufferedReader(new FileReader(textFile))) {
				for(String s; (s = in.readLine()) != null; lines.add(s));
			}
			catch(IOException e)
			{
				FMUM.log.error(
					I18n.format(
						"fmum.errorreadingtypefile",
						sourceTraceName
					),
					e
				);
				return null; // TODO: return a info type instance
			}
			
			// Instantiate typer
			T type;
			try
			{
				type = this.instantiator.newInstance(
					fName.substring(0, fName.length() - LocalContentProvider.TXT_SUFFIX.length()),
					packName
				);
			}
			catch(
				InstantiationException
				| IllegalAccessException
				| IllegalArgumentException
				| InvocationTargetException e
			) {
				throw new RuntimeException(
					I18n.format(
						"fmum.failedtoinstantiateitemtyper",
						this.instantiator.getName()
					),
					e
				);
			}
			
			// Parse from the lines
			return this.parse(lines, type, sourceTraceName);
		}
	}
}