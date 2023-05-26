package com.fmum.common.load;

import com.fmum.common.FMUM;
import com.fmum.common.meta.IMeta;
import com.google.gson.JsonObject;

import java.util.function.Function;

/**
 * Loader for specified {@link IBuildable}s.
 * 
 * @author Giant_Salted_Fish
 */
public final class BuildableLoader< T > implements IMeta // Implements this to use Registry<T>.
{
	public final String entry;
	
	public final Function< JsonObject, ? extends IBuildable< T > > parser;
	
	public BuildableLoader( String entry, Class< ? extends IBuildable< T > > clazz ) {
		this( entry, json -> FMUM.GSON.fromJson( json, clazz ) );
	}
	
	public BuildableLoader( String entry, Function< JsonObject, ? extends IBuildable< T > > parser )
	{
		this.entry = entry;
		this.parser = parser;
	}
	
	@Override
	public String name() { return this.entry; }
	
	@Override
	public String toString() { return this.entry.toUpperCase(); }
}
