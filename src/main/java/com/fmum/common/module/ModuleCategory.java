package com.fmum.common.module;

/**
 * @see CategoryDomain
 */
public class ModuleCategory
{
	public static final ModuleCategory END = new ModuleCategory()
	{
		@Override
		public int getMatchedDepth( ModuleCategory other ) {
			return 0;
		}
	};
	
	public final String value;
	public final ModuleCategory sub;
	
	/**
	 * Parse dot separated category string. For example {@code "ar.mag.drum"}.
	 */
	public ModuleCategory( String raw_category )
	{
		final int idx = raw_category.indexOf( '.' );
		if ( idx != -1 )
		{
			this.value = raw_category.substring( 0, idx );
			this.sub = new ModuleCategory( raw_category.substring( idx + 1 ) );
		}
		else
		{
			this.value = raw_category;
			this.sub = END;
		}
	}
	
	private ModuleCategory()
	{
		this.value = ".";
		this.sub = this;
	}
	
	public int getMatchedDepth( ModuleCategory other )
	{
		final boolean matched = this.value.equals( other.value );
		return matched ? this.sub.getMatchedDepth( other.sub ) + 1 : 0;
	}
	
	public final int depth()
	{
		int depth = 1;
		ModuleCategory sub = this.sub;
		while ( sub != END )
		{
			sub = sub.sub;
			depth += 1;
		}
		return depth;
	}
}
