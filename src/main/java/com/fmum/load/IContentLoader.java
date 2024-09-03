package com.fmum.load;

import com.fmum.Registry;
import com.google.gson.JsonObject;

import java.util.Arrays;

@FunctionalInterface
public interface IContentLoader
{
	/**
	 * @param obj JSON object of the content to load.
	 * @param fallback_name A fallback name provided by the source.
	 * @param ctx Context used to build up the content.
	 * @return The loaded content object.
	 */
	Object loadFrom( JsonObject obj, String fallback_name, IContentBuildContext ctx );
	
	
	@SafeVarargs
	static < T extends BuildableType > IContentLoader of(
		Class< T > type_clazz,
		Registry< ? super T >... registries
	) {
		return ( obj, fb_name, ctx ) -> {
			final T buildable = ctx.getGson().fromJson( obj, type_clazz );
			buildable.build( obj, fb_name, ctx );
			Arrays.stream( registries ).forEach( r -> r.regis( buildable.name, buildable ) );
			return buildable;
		};
	}
}
