package com.fmum.load;

import com.google.gson.Gson;
import com.google.gson.JsonDeserializer;

import java.lang.reflect.Type;
import java.util.Optional;

public interface ILoadContext
{
	/**
	 * A default {@link Gson} instance that can be used to serialize/deserialize
	 * JSON objects.
	 *
	 * @see IPreLoadContext#regisGsonDeserializer(Type, JsonDeserializer)
	 */
	Gson getGson();
	
	/**
	 * Lookup a content loader that registered with {@link IPreLoadContext#regisContentLoader(String, IContentLoader)}.
	 */
	Optional< IContentLoader > lookupContentLoader( String entry );
}
