package com.fmum.common.module;

public final class ModuleCategory
{
	public static final ModuleCategory END = new ModuleCategory();
	
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
}
