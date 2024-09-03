package com.fmum.load;

import com.google.gson.JsonDeserializer;

import java.lang.reflect.Type;

public interface IPreLoadContext
{
	/**
	 * Register a {@link com.google.gson.Gson} type adapter for deserialization.
	 *
	 * @see ILoadContext#getGson()
	 */
	void regisGsonDeserializer( Type type, JsonDeserializer< ? > adapter );
	
//	void regisGsonSerializer( Type type, JsonSerializer< ? > adapter );
	
	/**
	 * Register a content loader that can be used by all loadable content packs
	 * in following load phases.
	 */
	void regisContentLoader( String entry, IContentLoader loader );
}
