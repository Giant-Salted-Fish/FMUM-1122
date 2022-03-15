package com.fmum.common.pack;

import java.io.File;
import java.lang.reflect.InvocationTargetException;

import com.fmum.common.FMUM;
import com.fmum.common.FMUMClassLoader;

import net.minecraft.client.resources.I18n;

/**
 * Content provider that fetches its source from local disk
 * 
 * @author Giant_Salted_Fish
 */
public abstract class LocalContentProvider implements FMUMContentProvider
{
	public static final String
		TXT_SUFFIX = ".txt",
		CLASS_SUFFIX = ".class";
	
	/**
	 * Source file on local disk
	 */
	protected final File source;
	
	protected LocalContentProvider(File source)
	{
		this.source = source;
	}
	
	/**
	 * Register class path for this content pack. Additionally, register resource pack for client
	 * side. In default request {@link FMUM#proxy} to register resources.
	 */
	@Override
	public void prepareLoad() { FMUM.proxy.registerLocalResource(this.source); }
	
	@Override
	public String getSourceName() { return this.source.getName(); }
	
	protected static void iterateTypeFiles(File dir, TypeFileProcessor processor) {
		iterateTypeFiles(dir, dir.getName(), processor);
	}
	
	protected static void iterateTypeFiles(File dir, String classPath, TypeFileProcessor processor)
	{
		for(File f : dir.listFiles())
		{
			if(f.isDirectory())
				iterateTypeFiles(f, classPath + "." + f.getName(), processor);
			else processor.process(f, classPath);
		}
	}
	
	/**
	 * Try to load required class and instantiate it. Prints the error if an error has occurred
	 * 
	 * @param fName Name of the class file to load
	 * @param superClassPath Package path of the class to load
	 * @param packName Content pack name used in error print
	 */
	protected static void tryInstantiate(String fName, String superClassPath, String packName)
	{
		try
		{
			FMUMClassLoader.instance.loadClass(
				superClassPath + "."
					+ fName.substring(0, fName.length() - CLASS_SUFFIX.length())
			).getConstructor().newInstance();
		}
		catch(
			InstantiationException
			| IllegalAccessException
			| IllegalArgumentException
			| InvocationTargetException
			| NoSuchMethodException
			| SecurityException
			| ClassNotFoundException e
		) {
			FMUM.log.error(
				I18n.format(
					"fmum.errorloadingclassbaseditemtyper",
					packName + ":" + superClassPath + "." + fName
				),
				e
			);
		}
	}
	
	/**
	 * Used in {@link LocalContentProvider#iterateTypeFiles(File, TypeFileProcessor)} to process
	 * every type file met
	 * 
	 * @author Giant_Salted_Fish
	 */
	@FunctionalInterface
	public static interface TypeFileProcessor {
		public void process(File typeFile, String superClassPath);
	}
}
