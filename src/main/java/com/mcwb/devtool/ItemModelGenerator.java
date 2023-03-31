package com.mcwb.devtool;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public final class ItemModelGenerator
{
	public static void main( String[] args )
	{
		final String inDir = args.length < 1 ? "z-dev/src/" : args[ 0 ];
		final String outDir = args.length < 2 ? "z-dev/dst/" : args[ 1 ];
		
		processDir( new File( inDir ), new File( outDir ) );
	}
	
	private static void processDir( File srcDir, File dstDir )
	{
		for ( File file : srcDir.listFiles() )
		{
			final String fName = file.getName();
			
			if ( file.isDirectory() )
				processDir( file, dstDir );
			else if ( fName.endsWith( ".json" ) )
			{
				final File dstFile = new File( dstDir, fName );
				if ( dstFile.exists() )
					System.out.println( "Skipped file " + fName );
				else try ( BufferedWriter out = new BufferedWriter( new FileWriter( dstFile ) ) )
					{
						out.write(
							"{\n" +
							"\t" + "\"parent\": \"builtin/generated\",\n" +
							"\t" + "\"textures\": {\n" +
							"\t" + "\t" + "\"layer0\": \"mcwb:items/" + fName.substring( 0, fName.length() - 5 ) + "\"\n" +
							"\t" + "}\n" +
							"}\n"
						);
					}
					catch ( IOException e )
					{
						e.printStackTrace();
						System.exit( -1 );
					}
					System.out.println( "Gen item model for " + file.getName() );
			}
			else System.out.println( "Unrecognized file " + file.getName() );
		}
	}
}
