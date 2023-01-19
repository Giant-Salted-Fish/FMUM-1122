package com.mcwb.common.load;

import java.util.function.Function;

import com.google.gson.JsonObject;
import com.mcwb.common.MCWB;
import com.mcwb.common.meta.IMeta;

public final class BuildableLoader< T > implements IMeta // Implements this to use Registry<>
{
	public final String entry;
	
	public final Function< JsonObject, ? extends IBuildable< T > > parser;
	
	public BuildableLoader( String entry, Class< ? extends IBuildable< T > > clazz ) {
		this( entry, json -> MCWB.GSON.fromJson( json, clazz ) );
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
