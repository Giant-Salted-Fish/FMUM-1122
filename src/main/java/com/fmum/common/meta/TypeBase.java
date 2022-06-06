package com.fmum.common.meta;

import java.util.TreeMap;

import com.fmum.common.FMUM;
import com.fmum.common.pack.ContentProvider;
import com.fmum.common.util.LocalAttrParser;
import com.fmum.common.util.Util;

/**
 * A simple implementation of {@link MetaBase}
 * 
 * @author Giant_Salted_Fish
 */
public abstract class TypeBase implements MetaBase
{
	public static final LocalAttrParser< TypeBase > parser = new LocalAttrParser<>( null );
	static
	{
		parser.addKeyword( "Name", ( s, t ) -> { t.name = s[ 1 ]; } );
		
		// You can add multiple lines of description by using multiple "Description" keyword
		parser.addKeyword(
			"Description",
			( s, t ) -> {
				t.description = (
					t.description != DESCRIPTION_MISSING ? t.description + "\n" : ""
				) + Util.splice( s, 1 );
			}
		);
	}
	
	/**
	 * Identifier of this meta. Should be universally unique.
	 */
	public String name;
	
	/**
	 * Description of this meta
	 */
	public String description = DESCRIPTION_MISSING;
	
	/**
	 * Provider of the meta. Usually is the content pack that this meta belongs to.
	 */
	public ContentProvider provider = FMUM.MOD;
	
	public TypeBase( String name ) { this.name = name; }
	
	@Override
	public String name() { return this.name; }
	
	@Override
	public String description() { return this.description; }
	
	@Override
	public ContentProvider provider() { return this.provider; }
	
	@Override
	public final void $provider( ContentProvider provider )
	{
		if( this.provider != FMUM.MOD )
			throw new RuntimeException( "Provider of " + this + " has already been set!" );
		this.provider = provider;
	}
	
	@Override
	public String toString() { return this.identifier(); }
	
	protected static TreeMap< String, Integer > parseMaterial( String[] split, int cursor )
	{
		final TreeMap< String, Integer > ret = new TreeMap<>();
		while( cursor < split.length )
		{
			ret.put(
				split[ cursor ],
				cursor + 1 < split.length ? Integer.parseInt( split[ cursor + 1 ] ) : 1
			);
			cursor += 2;
		}
		return ret;
	}
}
