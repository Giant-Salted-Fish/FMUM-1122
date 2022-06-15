package com.fmum.common.pack;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import com.fmum.common.FMUM;
import com.fmum.common.type.TypeInfo;
import com.fmum.common.util.Messager;

/**
 * Content provider that fetches its source from local disk
 * 
 * @author Giant_Salted_Fish
 */
public abstract class LocalContentProvider implements FMUMContentProvider
{
	/**
	 * Source file on local disk
	 */
	protected final File source;
	
	public final ContentProviderSourceInfo info;
	
	protected LocalContentProvider(File source)
	{
		this.source = source;
		this.info = new ContentProviderSourceInfo(source.getName());
	}
	
	/**
	 * Register class path for this content pack. Additionally, register resource pack for client
	 * side. In default request {@link FMUM#proxy} to register resources.
	 */
	@Override
	public void prepareLoad() { FMUM.proxy.registerLocalResource(this.source); }
	
	@Override
	public String getSourceName() { return this.source.getName(); }
	
	@Override
	public ContentProviderSourceInfo getInfo() { return this.info; }
	
	protected void parseInfo(BufferedReader textInput, Messager sourceTrace)
	{
		try { ContentProviderSourceInfo.parser.parse(textInput, this.info, sourceTrace); }
		catch(IOException e) { printIOError(sourceTrace.message(), e); }
	}
	
	protected static boolean iterateTypeFiles(File dir, TypeFileProcessor processor) {
		return iterateTypeFiles(dir, dir.getName(), processor);
	}
	
	protected static boolean iterateTypeFiles(
		File dir,
		String classPath,
		TypeFileProcessor processor
	) {
		for(File f : dir.listFiles())
		{
			if(f.isDirectory())
				iterateTypeFiles(f, classPath + "." + f.getName(), processor);
			else if(processor.process(f, classPath))
				return true;
		}
		
		return false;
	}
	
	protected static boolean iterateTypeZipEntries(File zipFile, TypeZipEntryProcessor processor)
	{
		try(
			ZipInputStream zipIn = new ZipInputStream(new FileInputStream(zipFile));
			BufferedReader in = new BufferedReader(new InputStreamReader(zipIn));
		) {
			for(ZipEntry entry = zipIn.getNextEntry(); (entry = zipIn.getNextEntry()) != null; )
				if(processor.process(entry, in))
					return true;
		}
		catch(IOException e) { printIOError(zipFile.getName(), e); }
		return false;
	}
	
	/**
	 * Try to load typer class and instantiate it. Prints the error if any has present. Notice that
	 * this method will notice {@code this} as the provider of loaded {@link TypeInfo}.
	 * 
	 * @param sourceTrace Source trace name used in error print
	 * @param classPathFragments Class path fragments
	 */
	protected void loadClassBasedTyper(Messager sourceTrace, String... classPathFragments)
	{
		try { ((TypeInfo)FMUM.tryInstantiate(classPathFragments)).notifyProvider(this); }
		catch(Exception e)
		{
			FMUM.log.error(
				FMUM.proxy.format(
					"fmum.errorloadingclassbasedtyper",
					sourceTrace.message()
				),
				e
			);
		}
	}
	
	protected static void printIOError(String sourceTrace, IOException e) {
		FMUM.log.error(FMUM.proxy.format("fmum.ioerrorreadingfrom", sourceTrace), e);
	}
	
	protected static void printUnrecognizedType(String sourceTrace) {
		FMUM.log.error(FMUM.proxy.format("fmum.unrecognizedtypefiletype", sourceTrace));
	}
	
	/**
	 * Used in {@link LocalContentProvider#iterateTypeFiles(File, TypeFileProcessor)} to process
	 * every type file met
	 * 
	 * @author Giant_Salted_Fish
	 */
	@FunctionalInterface
	public static interface TypeFileProcessor
	{
		/**
		 * @return {@code true} to stop further iteration
		 */
		public boolean process(File typeFile, String superClassPath);
	}
	
	@FunctionalInterface
	public static interface TypeZipEntryProcessor
	{
		/**
		 * @return {@code true} to stop further iteration
		 */
		public boolean process(ZipEntry entry, BufferedReader in);
	}
}
