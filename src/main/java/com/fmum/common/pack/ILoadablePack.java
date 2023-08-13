package com.fmum.common.pack;

import com.google.gson.Gson;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonObject;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.io.File;
import java.lang.reflect.Type;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

public interface ILoadablePack
{
	/**
	 * Load pack info, regis necessary {@link Gson} adapters and content loaders.
	 *
	 * @see #prepareLoadClientSide(IPrepareContext)
	 */
	Function< ILoadContext, Supplier< IContentPack > > prepareLoadServerSide( IPrepareContext ctx);
	
	/**
	 * Load pack info, regis necessary {@link Gson} adapters and content loaders.
	 *
	 * @see #prepareLoadServerSide(IPrepareContext)
	 */
	@SideOnly( Side.CLIENT )
	Function< ILoadContext, Supplier< IContentPack > > prepareLoadClientSide( IPrepareContext ctx );
	
	interface IPrepareContext
	{
		void regisGsonAdapter( Type type, JsonDeserializer< ? > adapter );
		
		default < T > void regisContentLoader(
			String entry,
			Class< T > clazz,
			BiFunction< T, IBuildContext, ? > processor
		) {
			final IContentLoader loader = ( obj, gson, ctx ) -> {
				final T instance = gson.fromJson( obj, clazz );
				return processor.apply( instance, ctx );
			};
			this.regisContentLoader( entry, loader );
		}
		
		void regisContentLoader( String entry, IContentLoader loader );
		
		< T > void regisCapability( Class< T > capability_class );
	}
	
	interface ILoadContext
	{
		Gson gson();
		
		default Object loadContent( String loader_entry, JsonObject object, IBuildContext ctx )
			throws LoaderNotFoundException
		{
			return this.getContentLoader( loader_entry )
				.orElseThrow( LoaderNotFoundException::new )
				.loadFrom( object, this.gson(), ctx );
		}
		
		Optional< IContentLoader > getContentLoader( String entry );
	}
	
	@FunctionalInterface
	interface IContentLoader {
		Object loadFrom( JsonObject obj, Gson gson, IBuildContext ctx );
	}
	
	interface IBuildContext
	{
		String fallbackName();
		
		IContentPack contentPack();
		
		Gson gson();
		
		void regisPostLoadCallback( Runnable callback );
		
//		ResourceLocation loadTexture( String path );
	}
}
