package com.fmum.common.module;

import java.util.Set;
import java.util.TreeSet;

import com.fmum.common.util.CoordSystem;
import com.fmum.common.util.Vec3;

/**
 * An implementation of {@link ModuleSlot} with a fixed position. Usually used for modification of
 * cars.
 * 
 * @see ModuleSlot
 * @author Giant_Salted_Fish
 */
public class FixedSlot extends Vec3 implements ModuleSlot
{
	protected static final TreeSet< String > DEF_LIST = new TreeSet<>();
	
	/**
	 * Maximum number of attachments can be attached to this slot
	 */
	public byte maxCanInstall = 1;
	
	/**
	 * White list and blacklist for what can install and what are banned
	 */
	public Set< String >
		categoryWhitelist = DEF_LIST,
		categoryBlacklist = DEF_LIST,
		moduleWhitelist = DEF_LIST,
		moduleBlacklist = DEF_LIST;
	
	@Override
	public FixedSlot rescale( double s )
	{
		super.scale( s );
		return this;
	}
	
	@Override
	public boolean isAllowed( MetaModular module )
	{
		final String name = module.name();
		return(
			this.moduleWhitelist.size() > 0
			? this.moduleWhitelist.contains( name )
			: (
				!this.moduleBlacklist.contains( name )
				&& (
					this.categoryWhitelist.size() > 0
					? this.categoryWhitelist.contains( name )
					: !this.categoryBlacklist.contains( name )
				)
			)
		);
	}
	
	@Override
	public int maxCanInstall() { return this.maxCanInstall; }
	
	@Override
	public void applyTransform( CoordSystem sys ) { sys.trans( this ); }
	
	// TODO: a parser maybe?
	
	protected static int parseList( String[] split, String close, int cursor, Set< String > dst )
	{
		while( !split[ ++cursor ].equals( close ) )
			dst.add( split[ cursor ] );
		return cursor;
	}
}
