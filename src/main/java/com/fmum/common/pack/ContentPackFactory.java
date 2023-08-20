package com.fmum.common.pack;

import com.fmum.common.load.ContentBuildContext;
import com.fmum.common.load.ContentLoader;
import com.fmum.common.load.LoaderNotFoundException;
import com.fmum.common.tab.CreativeTab;
import com.google.gson.Gson;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonObject;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.lang.reflect.Type;
import java.util.Optional;
import java.util.function.Consumer;

public interface ContentPackFactory
{
	/**
	 * Load pack info, regis necessary {@link Gson} adapters and content loaders.
	 *
	 * @see #createClientSide(IPrepareContext)
	 */
	ContentPack createServerSide( IPrepareContext ctx );
	
	/**
	 * Load pack info, regis necessary {@link Gson} adapters and content loaders.
	 *
	 * @see #createServerSide(IPrepareContext)
	 */
	@SideOnly( Side.CLIENT )
	ContentPack createClientSide( IPrepareContext ctx );
	
	interface IPrepareContext
	{
		void regisLoadCallback( Consumer< ILoadContext > callback );
		
		void regisPostLoadCallback( Consumer< IPostLoadContext > callback );
		
		void regisGsonAdapter( Type type, JsonDeserializer< ? > adapter );
		
		void regisContentLoader( String entry, ContentLoader loader );
		
		< T > void regisCapability( Class< T > capability_class );
	}
	
	interface ILoadContext
	{
		void regisPostLoadCallback( Consumer< IPostLoadContext > callback );
		
		Gson gson();
		
		default Object loadContent(
			String loader_entry, JsonObject object, ContentBuildContext ctx
		) throws LoaderNotFoundException
		{
			return this.getContentLoader( loader_entry )
				.orElseThrow( LoaderNotFoundException::new )
				.loadFrom( object, this.gson(), ctx );
		}
		
		Optional< ContentLoader > getContentLoader( String entry );
	}
	
	interface IPostLoadContext
	{
		@SideOnly( Side.CLIENT )
		ItemStack defaultTabIconItem();
		
		CreativeTab defaultCreativeTab();
		
		CreativeTab hideCreativeTab();
	}
}
