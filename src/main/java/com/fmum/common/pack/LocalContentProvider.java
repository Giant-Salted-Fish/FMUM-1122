package com.fmum.common.pack;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import com.fmum.common.AutowireLogger;
import com.fmum.common.FMUM;
import com.fmum.common.FMUMClassLoader;
import com.fmum.common.meta.MetaBase;
import com.fmum.common.util.Messager;

/**
 * Content provider that fetches its source from local disk
 * 
 * @author Giant_Salted_Fish
 */
public abstract class LocalContentProvider implements ContentProvider, AutowireLogger
{
	public final ContentPackInfo info;
	
	/**
	 * Source file on local disk
	 */
	protected final File source;
	
	protected LocalContentProvider( File source )
	{
		this.source = source;
		this.info = new ContentPackInfo( source.getName() );
	}
	
	/**
	 * Register class path for this content pack. Additionally, register resource pack for client
	 * side.
	 */
	@Override
	public void prepareLoad() { FMUM.MOD.regisLocalResource( this.source ); }
	
	@Override
	public String name() { return this.info.name; }
	
	@Override
	public String author() { return this.info.author; }
	
	@Override
	public String description() { return this.info.description; }
	
	@Override
	public String sourceName() { return this.source.getName(); }
	
	protected void parseInfo( BufferedReader textInput, Messager sourceTrace )
	{
		try { ContentPackInfo.parser.parse( textInput, this.info, sourceTrace ); }
		catch( IOException e ) { this.printIOError( sourceTrace.message(), e ); }
		catch( Exception e ) { this.printUnexpectedError( sourceTrace.message(), e ); }
	}
	
	protected boolean iterateTypeFiles( File dir, FileConsumer processor ) {
		return this.iterateTypeFiles( dir, dir.getName(), processor );
	}
	
	protected boolean iterateTypeFiles( File dir, String classPath, FileConsumer processor )
	{
		for( File f : dir.listFiles() )
			if( f.isDirectory() )
				this.iterateTypeFiles( f, classPath + "." + f.getName(), processor );
			else if( processor.process( f, classPath ) )
				return true;
		
		return false;
	}
	
	protected boolean iterateZipEntries( File zipFile, ZipEntryConsumer processor )
	{
		try(
			ZipInputStream zipIn = new ZipInputStream( new FileInputStream( zipFile ) );
			BufferedReader in = new BufferedReader( new InputStreamReader( zipIn ) );
		) {
			for( ZipEntry entry = zipIn.getNextEntry(); ( entry = zipIn.getNextEntry() ) != null; )
				if( processor.process( entry, in ) )
					return true;
		}
		catch( IOException e ) { this.printIOError( zipFile.getName(), e ); }
		return false;
	}
	
	/**
	 * Try to load typer class and instantiate it. Prints the error if any has present.
	 * 
	 * @param sourceTrace Source trace name used in error print
	 * @param classPathFragments Class path fragments
	 */
	protected MetaBase loadClassBasedMeta( Messager sourceTrace, String... classPathFragments )
	{
		try {
			return ( ( MetaBase ) FMUMClassLoader.INSTANCE.tryInstantiate( classPathFragments ) );
		}
		catch( Exception e )
		{
			this.log().error(
				this.format(
					"fmum.errorloadingclassbasedtype",
					sourceTrace.message()
				),
				e
			);
		}
		return null;
	}
	
	protected void printUnknownFileType( String sourceTrace ) {
		this.log().error( this.format( "fmum.unknownfiletype", sourceTrace ) );
	}
	
	protected void printIOError( String sourceTrace, IOException e ) {
		this.log().error( this.format( "fmum.ioerror", sourceTrace ), e );
	}
	
	protected void printUnexpectedError( String sourceTrace, Exception e ) {
		this.log().error( this.format( "fmum.unexpectederror", sourceTrace ), e );
	}
	
	/**
	 * Used in {@link LocalContentProvider#iterateTypeFiles(File, FileConsumer)} to process
	 * every type file met
	 * 
	 * @author Giant_Salted_Fish
	 */
	@FunctionalInterface
	public static interface FileConsumer
	{
		/**
		 * @return {@code true} to stop further iteration
		 */
		public boolean process( File typeFile, String superClassPath );
	}
	
	@FunctionalInterface
	public static interface ZipEntryConsumer
	{
		/**
		 * @return {@code true} to stop further iteration
		 */
		public boolean process( ZipEntry entry, BufferedReader in );
	}
}
