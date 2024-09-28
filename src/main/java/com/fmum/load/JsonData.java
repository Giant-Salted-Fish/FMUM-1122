package com.fmum.load;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public final class JsonData
{
	private final JsonObject data;
	private final Gson gson;
	
	public JsonData( JsonObject data, Gson gson )
	{
		this.data = data;
		this.gson = gson;
	}
	
	public Optional< JsonElement > get( String key ) {
		return Optional.ofNullable( this.data.get( key ) );
	}
	
	public Optional< Integer > getInt( String key ) {
		return this.get( key ).map( JsonElement::getAsInt );
	}
	
	public Optional< Float > getFloat( String key ) {
		return this.get( key ).map( JsonElement::getAsFloat );
	}
	
	public Optional< Boolean > getBool( String key ) {
		return this.get( key ).map( JsonElement::getAsBoolean );
	}
	
	public Optional< String > getString( String key ) {
		return this.get( key ).map( JsonElement::getAsString );
	}
	
	public < T > Optional< T > get( String key, Class< T > cls ) {
		return this.get( key ).map( e -> this.gson.fromJson( e, cls ) );
	}
	
	public < T > Optional< List< T > > getList( String key, Class< T > cls )
	{
		final ParameterizedType type = __make( List.class, cls );
		return this.get( key ).map( e -> this.gson.fromJson( e, type ) );
	}
	
	public < T > Optional< Predicate< T > > getPredicate( String key, Class< T > cls )
	{
		final ParameterizedType type = __make( Predicate.class, cls );
		return this.get( key ).map( e -> this.gson.fromJson( e, type ) );
	}
	
	private static ParameterizedType __make( Class< ? > raw, Type... args )
	{
		return new ParameterizedType() {
			@Override
			public Type[] getActualTypeArguments() {
				return args;
			}
			
			@Override
			public Type getRawType() {
				return raw;
			}
			
			@Override
			public Type getOwnerType() {
				return raw.getDeclaringClass();
			}
		};
	}
}
