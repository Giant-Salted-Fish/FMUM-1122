package gsf.fmum.util;

/**
 * @see CategoryDomain
 */
public class Category
{
	public static final Category END = new Category()
	{
		@Override
		public int getMatchedDepth( Category other ) {
			return 0;
		}
	};
	
	public final String value;
	public final Category sub;
	
	/**
	 * Parse dot separated category string. For example {@code "ar.mag.drum"}.
	 */
	public Category( String raw_category )
	{
		final int idx = raw_category.indexOf( '.' );
		if ( idx != -1 )
		{
			this.value = raw_category.substring( 0, idx );
			this.sub = new Category( raw_category.substring( idx + 1 ) );
		}
		else
		{
			this.value = raw_category;
			this.sub = END;
		}
	}
	
	private Category()
	{
		this.value = ".";
		this.sub = this;
	}
	
	public int getMatchedDepth( Category other )
	{
		final boolean matched = this.value.equals( other.value );
		return matched ? this.sub.getMatchedDepth( other.sub ) + 1 : 0;
	}
	
	public final int depth()
	{
		int depth = 1;
		Category sub = this.sub;
		while ( sub != END )
		{
			sub = sub.sub;
			depth += 1;
		}
		return depth;
	}
}
