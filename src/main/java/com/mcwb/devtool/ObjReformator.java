package com.mcwb.devtool;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import net.minecraftforge.client.model.obj.OBJLoader;

/**
 * <p> Do two things: </p>
 * <ol>
 *     <li> Ensure all "vt" value is inside of {@code 0F-1F} (required by {@link OBJLoader} </li>
 *     <li> Comment "mtllib" and "usemtl" if represent </li>
 * </ol>
 * 
 * @author Giant_Salted_Fish
 */
public final class ObjReformator
{
	public static void main( String[] args )
	{
		final String inDir = args.length < 1 ? "z-dev/" : args[ 0 ];
		final String outDir = args.length < 2 ? inDir + "dst/" : args[ 1 ];
		
		System.out.println( "In Dir: " + inDir );
		System.out.println( "Out Dir: " + outDir );
		
		new File( outDir ).mkdirs();
		
		for( File file : new File( inDir ).listFiles() )
		{
			// Only process .obj files
			final String fName = file.getName();
			if( !fName.endsWith( ".obj" ) )
				continue;
			
			// If has .obj.obj suffix then remove it
			final String outFileName = fName.endsWith( ".obj.obj" )
				? fName.substring( 0, fName.length() - 4 ) : fName;
			process( inDir + fName, outDir + outFileName );
			System.out.println( "Complete " + fName );
		}
		
		System.out.println( "All done" );
	}
	
	private static void process( String inPath, String outPath )
	{
		try(
			BufferedReader in = new BufferedReader( new FileReader( inPath ) );
			BufferedWriter out = new BufferedWriter( new FileWriter( outPath ) );
		) {
			for( String line; ( line = in.readLine() ) != null; out.newLine() )
			{
				if( line.startsWith( "vt" ) )
				{
					final String[] split = line.split( " " );
					final double[] value = new double[ split.length - 1 ];
					for(
						int i = split.length;
						--i > 0;
						value[ i - 1 ] = Double.parseDouble( split[ i ] )
					);
					
					int i = value.length;
					while( i-- > 0 )
						if( value[ i ] < 0D || value[ i ] > 1D )
						{
							out.write( "vt" );
							for( int j = 0; j < value.length; ++j )
							{
								double val = value[ j ];
								for(
									final double step = value[ j ] < 0D ? 1D : -1D;
									val < 0D || val > 1D;
									val += step
								);
								out.write( " " + val );
							}
							break;
						}
					
					if( i < 0 )
						out.write( line );
				}
				else
				{
					// Remove "mtllib" and "usemtl" label
					if( line.startsWith( "mtllib" ) || line.startsWith( "usemtl" ) )
						out.write( '#' );
					out.write( line );
				}
			}
		}
		catch( IOException e ) { e.printStackTrace(); }
	}
}
