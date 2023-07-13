package com.fmum.common.pack;

import com.google.gson.Gson;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonObject;

import javax.annotation.Nullable;
import java.io.File;
import java.lang.reflect.Type;
import java.util.function.BiFunction;

public interface IContentPack
{
	void prepareLoad( IPrepareContext ctx );
	
	void loadContent( ILoadContext ctx );
	
	String name();
	
	String author();
	
	/**
	 * Used in error handling procedure to give human-readable names that can be used to identify
	 * problems.
	 */
	String sourceName();
	
	interface IPrepareContext
	{
		void regisGsonAdapter( Type type, JsonDeserializer< ? > adapter );
		
		default void regisTypeLoader( String entry, Class< ? extends IBuildable< ? > > clazz ) {
			this.regisTypeLoader( entry, ( obj, gson ) -> gson.fromJson( obj, clazz ).build() );
		}
		
		void regisTypeLoader( String entry, BiFunction< JsonObject, Gson, ? > loader );
		
		void regisResourceDomain( File file );
		
		< T > void regisCapability( Class< T > capability_class );
	}
	
	interface ILoadContext
	{
		Gson gson();
		
		default Object loadType( String loader_entry, JsonObject object )
			throws LoaderNotFoundException
		{
			final BiFunction< JsonObject, Gson, ? > loader = this.getTypeLoader( loader_entry );
			if ( loader == null ) {
				throw new LoaderNotFoundException();
			}
			
			return loader.apply( object, this.gson() );
		}
		
		@Nullable
		BiFunction< JsonObject, Gson, ? > getTypeLoader( String entry );
	}
}
