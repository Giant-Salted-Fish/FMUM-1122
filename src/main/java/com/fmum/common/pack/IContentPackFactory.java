package com.fmum.common.pack;

import com.fmum.common.tab.ICreativeTab;
import com.google.gson.Gson;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonObject;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.lang.reflect.Type;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Consumer;

public interface IContentPackFactory
{
	/**
	 * Load pack info, regis necessary {@link Gson} adapters and content loaders.
	 *
	 * @see #createClientSide(IPrepareContext)
	 */
	IContentPack createServerSide( IPrepareContext ctx );
	
	/**
	 * Load pack info, regis necessary {@link Gson} adapters and content loaders.
	 *
	 * @see #createServerSide(IPrepareContext)
	 */
	@SideOnly( Side.CLIENT )
	IContentPack createClientSide( IPrepareContext ctx );
	
	interface IPrepareContext
	{
		void regisLoadCallback( Consumer< ILoadContext > callback );
		
		void regisPostLoadCallback( Consumer< IPostLoadContext > callback );
		
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
	
	interface ILoadContext
	{
		void regisPostLoadCallback( Consumer< IPostLoadContext > callback );
		
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
	
	interface IPostLoadContext
	{
		@SideOnly( Side.CLIENT )
		ItemStack defaultTabIconItem();
		
		ICreativeTab defaultCreativeTab();
		
		ICreativeTab hideCreativeTab();
	}
}
