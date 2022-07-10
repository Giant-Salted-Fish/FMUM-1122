package com.fmum.common.pack;

import com.fmum.common.Meta;
import com.fmum.common.util.Util;

/**
 * Store the basic information of a content pack
 * 
 * @author Giant_Salted_Fish
 */
public class ContentPackInfo
{
	public static final String RECOMMENDED_FILE_NAME = "pack.txt";
	
	public static final TypeParser< ContentPackInfo >
		parser = new TypeParser<>( ContentPackInfo.class, null );
	static
	{
		parser.addKeyword( "Name", ( s, d ) -> d.name = s[ 1 ] );
		parser.addKeyword( "Author", ( s, d ) -> d.author = s[ 1 ] );
		parser.addKeyword(
			"Description",
			( s, t ) -> {
				t.description = (
					t.description != Meta.DESCRIPTION_MISSING ? t.description + "\n" : ""
				) + Util.splice( s, 1 );
			}
		);
	}
	
	public String name;
	
	public String author = Meta.AUTHOR_MISSING;
	
	public String description = Meta.DESCRIPTION_MISSING;
	
	public ContentPackInfo( String name ) { this.name = name; }
}
