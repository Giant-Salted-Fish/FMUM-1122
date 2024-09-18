package com.fmum.load;

import com.fmum.Registry;
import com.google.gson.JsonObject;
import com.google.gson.annotations.Expose;

/**
 * A template buildable content type class that provides name, pack info, and
 * default {@link #toString()} implementation.
 *
 * @see IContentLoader#of(Class, Registry[])
 */
public abstract class BuildableType
{
	/**
	 * If the name is not specified, then the fallback name (usually is the file
	 * name) will be used.
	 */
	@Expose
	protected String name;
	
	
	/**
	 * The info of the pack that this type is loaded from.
	 */
	protected IPackInfo pack_info;
	
	protected BuildableType() { }
	
	public void build( JsonObject data, String fallback_name, IContentBuildContext ctx )
	{
		if ( this.name == null ) {
			this.name = fallback_name;
		}
		this.pack_info = ctx.getPackInfo();
	}
	
	// TODO: Add method to support runtime reload?
	
	@Override
	public String toString()
	{
		final String type = this.getClass().getSimpleName();
		return String.format( "%s::<%s.%s>", type, this.pack_info, this.name );
	}
}
