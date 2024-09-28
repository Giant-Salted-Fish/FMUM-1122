package com.fmum.load;

import com.fmum.Registry;

import java.util.Arrays;
import java.util.Optional;
import java.util.function.Supplier;

public interface IContentLoader< T >
{
	/**
	 * @param data JSON object of the content to load.
	 * @param fallback_name A fallback name provided by the source.
	 * @param ctx Context used to build up the content.
	 * @return The loaded content object.
	 */
	T load( JsonData data, String fallback_name, IContentBuildContext ctx );
	
	/**
	 * @see #load(JsonData, String, IContentBuildContext)
	 * @return Empty if this type of content does not support reloading.
	 */
	Optional< ? extends T > reload( JsonData data, String fallback_name, IContentBuildContext ctx );
	
	
	@SafeVarargs
	static < T extends BuildableType > IContentLoader< T > of(
		Supplier< ? extends T > factory,
		Registry< ? super T >... registries
	) {
		return new IContentLoader< T >() {
			@Override
			public T load( JsonData data, String fallback_name, IContentBuildContext ctx )
			{
				final T buildable = factory.get();
				buildable.build( data, fallback_name, ctx );
				Arrays.stream( registries ).forEach( r -> r.regis( buildable.name, buildable ) );
				return buildable;
			}
			
			@Override
			@SuppressWarnings( "unchecked" )
			public Optional< ? extends T > reload( JsonData data, String fallback_name, IContentBuildContext ctx )
			{
				if ( registries.length == 0 ) {
					return Optional.empty();
				}
				
				final Registry< ? super T > registry = registries[ 0 ];
				return registry.lookup( fallback_name ).map( obj -> {
					final T buildable = ( T ) obj;
					buildable.reload( data, ctx );
					return buildable;
				} );
			}
		};
	}
}
