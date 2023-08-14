package com.fmum.common.pack;

import com.google.gson.Gson;
import com.google.gson.JsonDeserializer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.lang.reflect.Type;
import java.util.function.BiFunction;

public interface ILoadablePack
{
	/**
	 * Load pack info, regis necessary {@link Gson} adapters and content loaders.
	 *
	 * @see #prepareLoadClientSide(IPrepareContext)
	 */
	IPreparedPack prepareLoadServerSide( IPrepareContext ctx );
	
	/**
	 * Load pack info, regis necessary {@link Gson} adapters and content loaders.
	 *
	 * @see #prepareLoadServerSide(IPrepareContext)
	 */
	@SideOnly( Side.CLIENT )
	IPreparedPack prepareLoadClientSide( IPrepareContext ctx );
	
	interface IPrepareContext
	{
		void regisGsonAdapter( Type type, JsonDeserializer< ? > adapter );
		
		default < T > void regisContentLoader(
			String entry,
			Class< T > clazz,
			BiFunction< T, IContentBuildContext, ? > processor
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
}
