package com.fmum.load;

import com.fmum.Registry;
import com.google.gson.JsonObject;

import java.util.function.Supplier;

/**
 * A template buildable content type class that provides name, pack info, and
 * default {@link #toString()} implementation.
 *
 * @see IContentLoader#of(Supplier, Registry[])
 */
public abstract class BuildableType
{
	/**
	 * If the name is not specified, then the fallback name (usually is the file
	 * name) will be used.
	 */
	protected String name;
	
	
	/**
	 * The info of the pack that this type is loaded from.
	 */
	protected IPackInfo pack_info;
	
	protected BuildableType() { }
	
	public void build( JsonObject data, String fallback_name, IContentBuildContext ctx )
	{
		this.name = fallback_name;
		this.pack_info = ctx.getPackInfo();
		this.reload( data, ctx );
	}
	
	public void reload( JsonObject data, IContentBuildContext ctx ) {
		// Pass.
	}
	
	@Override
	public String toString()
	{
		final String type = this.getClass().getSimpleName();
		return String.format( "%s::<%s.%s>", type, this.pack_info, this.name );
	}
}
