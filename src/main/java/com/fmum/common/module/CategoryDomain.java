package com.fmum.common.module;

import com.google.gson.JsonElement;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;

/**
 * This is used by {@link IModuleSlot} to check compatibility of the module that is going to be
 * installed.
 *
 * @see ModuleCategory
 */
public final class CategoryDomain
{
	public static final CategoryDomain DEFAULT = new CategoryDomain();
	
	private boolean compatibility_flag = false;
	
	private Map< String, CategoryDomain > sub_domains = Collections.emptyMap();
	
	public CategoryDomain( JsonElement element )
	{
		// To reduce duplicate code when handling JSON arr and obj, the main process flow is \
		// extracted and reused in both cases.
		final BiConsumer< String, Boolean > processer = ( raw_category, flag ) -> {
			CategoryDomain domain = this;
			ModuleCategory category = new ModuleCategory( raw_category );
			while ( category != ModuleCategory.END )
			{
				final CategoryDomain sub_domain = new CategoryDomain();
				domain.__addSubDomain( category.value, sub_domain );
				
				domain = sub_domain;
				category = category.sub;
			}
			
			domain.compatibility_flag = flag;
		};
		
		if ( element.isJsonArray() )
		{
			element.getAsJsonArray().forEach(
				e -> processer.accept( e.getAsString(), true ) );
		}
		else if ( element.isJsonObject() )
		{
			element.getAsJsonObject().entrySet().forEach(
				e -> processer.accept( e.getKey(), e.getValue().getAsBoolean() ) );
		}
		else {
			throw new RuntimeException( "Unsupported JSON element type." );
		}
	}
	
	private CategoryDomain() { }
	
	public boolean isCompatible( ModuleCategory category )
	{
		return Optional.ofNullable( this.sub_domains.get( category.value ) )
			.map( filter -> filter.isCompatible( category.sub ) )
			.orElse( this.compatibility_flag );
	}
	
	private void __addSubDomain( String identifier, CategoryDomain sub_domain )
	{
		if ( this.sub_domains.isEmpty() ) {
			this.sub_domains = new HashMap<>(); }
		this.sub_domains.put( identifier, sub_domain );
	}
}
