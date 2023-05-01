package com.fmum.common.load;

import java.util.function.Function;

import com.fmum.common.FMUM;
import com.fmum.common.meta.IMeta;
import com.google.gson.JsonObject;

/**
 * Loader for specified {@link IBuildable}s.
 * 
 * @author Giant_Salted_Fish
 */
public final class BuildableLoader< T > implements IMeta // Implements this to use Registry<T>.
{
	public final String entry;
	
	public final Function< JsonObject, ? extends IBuildable< ? extends T > > parser;
	
	public BuildableLoader( String entry, Class< ? extends IBuildable< ? extends T > > clazz ) {
		this( entry, json -> FMUM.GSON.fromJson( json, clazz ) );
	}
	
	public BuildableLoader(
		String entry,
		Function< JsonObject, ? extends IBuildable< ? extends T > > parser
	) {
		this.entry = entry;
		this.parser = parser;
	}
	
	@Override
	public String name() { return this.entry; }
	
	@Override
	public String toString() { return this.entry.toUpperCase(); }
}
