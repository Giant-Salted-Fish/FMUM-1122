package com.fmum.load;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * A wrapper class for {@link JsonObject} that provides a more convenient way to
 * access data.
 */
public final class JsonData
{
	public final JsonObject obj;
	public final JsonDeserializationContext ctx;
	
	public JsonData( JsonObject obj, JsonDeserializationContext ctx )
	{
		this.obj = obj;
		this.ctx = ctx;
	}
	
	public int size() {
		return this.obj.size();
	}
	
	public Optional< JsonElement > get( String key ) {
		return Optional.ofNullable( this.obj.get( key ) );
	}
	
	public Optional< JsonData > getData( String key ) {
		return this.get( key ).map( e -> new JsonData( e.getAsJsonObject(), this.ctx ) );
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
		return this.get( key ).map( e -> this.ctx.deserialize( e, cls ) );
	}
	
	public < T > Optional< List< T > > getList( String key, Class< T > cls )
	{
		final ParameterizedType type = __make( List.class, cls );
		return this.get( key ).map( e -> this.ctx.deserialize( e, type ) );
	}
	
	public < T > Optional< Predicate< T > > getPredicate( String key, Class< T > cls )
	{
		final ParameterizedType type = __make( Predicate.class, cls );
		return this.get( key ).map( e -> this.ctx.deserialize( e, type ) );
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
