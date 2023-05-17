package com.fmum.common.module;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

import com.google.gson.JsonElement;

public class ModuleFilter
{
	public static final ModuleFilter DEFAULT = new ModuleFilter();
	
	protected boolean isAllowed = false;
	
	protected Map< String, ModuleFilter > subDomains = Collections.emptyMap();
	
	public ModuleFilter( JsonElement element )
	{
		final BiConsumer< String, Boolean > processor = ( categoryStr, isAllowed ) -> {
			ModuleFilter filter = this;
			ModuleCategory category = new ModuleCategory( categoryStr );
			while ( category != ModuleCategory.END )
			{
				final ModuleFilter subFilter = new ModuleFilter();
				filter.addSubDomain( category.value, subFilter );
				
				filter = subFilter;
				category = category.sub;
			}
			
			filter.isAllowed = isAllowed;
		};
		
		if ( element.isJsonArray() )
		{
			element.getAsJsonArray().forEach(
				e -> processor.accept( e.getAsString(), true )
			);
		}
		else if ( element.isJsonObject() )
		{
			element.getAsJsonObject().entrySet().forEach(
				e -> processor.accept( e.getKey(), e.getValue().getAsBoolean() )
			);
		}
		else { throw new RuntimeException( "Unsupported json element type." ); }
	}
	
	private ModuleFilter() { }
	
	public boolean isAllowed( ModuleCategory category )
	{
		final ModuleFilter filter = this.subDomains.get( category.value );
		return filter != null ? filter.isAllowed( category.sub ) : this.isAllowed;
	}
	
	private void addSubDomain( String domainName, ModuleFilter filter )
	{
		if ( this.subDomains.size() == 0 ) { this.subDomains = new HashMap<>(); }
		this.subDomains.put( domainName, filter );
	}
}
