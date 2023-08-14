package com.fmum.common.pack;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.util.Optional;

@FunctionalInterface
public interface IPreparedPack
{
	ILoadedPack loadPack( ILoadContext ctx );
	
	interface ILoadContext
	{
		Gson gson();
		
		default Object loadContent(
			String loader_entry, JsonObject object, IContentBuildContext ctx
		) throws LoaderNotFoundException
		{
			return this.getContentLoader( loader_entry )
				.orElseThrow( LoaderNotFoundException::new )
				.loadFrom( object, this.gson(), ctx );
		}
		
		Optional< IContentLoader > getContentLoader( String entry );
	}
}
