package com.fmum.common.module;

public class ModuleCategory
{
	public static final ModuleCategory END = new ModuleCategory() {
		@Override
		public int getMatchingLayerCount( ModuleCategory other ) { return 0; }
	};
	
	public final String value;
	public final ModuleCategory sub;
	
	public ModuleCategory( String rawCategory )
	{
		final int idx = rawCategory.indexOf( '.' );
		if ( idx != -1 )
		{
			this.value = rawCategory.substring( 0, idx );
			this.sub = new ModuleCategory( rawCategory.substring( idx + 1 ) );
		}
		else
		{
			this.value = rawCategory;
			this.sub = END;
		}
	}
	
	private ModuleCategory()
	{
		this.value = ".";
		this.sub = this;
	}
	
	public int getMatchingLayerCount( ModuleCategory other )
	{
		final boolean matched = this.value.equals( other.value );
		return matched ? this.sub.getMatchingLayerCount( other.sub ) + 1 : 0;
	}
}
