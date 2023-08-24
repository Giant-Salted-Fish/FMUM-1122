package com.fmum.common.pack;

import com.fmum.common.load.ContentBuildContext;
import com.fmum.common.load.ContentLoader;
import com.fmum.common.load.LoaderNotFoundException;
import com.fmum.common.tab.CreativeTab;
import com.google.gson.Gson;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializer;
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
	 * @see #createClientSide(PrepareContext)
	 */
	ContentPack createServerSide( PrepareContext ctx );
	
	/**
	 * Load pack info, regis necessary {@link Gson} adapters and content loaders.
	 *
	 * @see #createServerSide(PrepareContext)
	 */
	@SideOnly( Side.CLIENT )
	ContentPack createClientSide( PrepareContext ctx );
	
	interface PrepareContext
	{
		void regisLoadCallback( Consumer< LoadContext > callback );
		
		void regisPostLoadCallback( Consumer< PostLoadContext > callback );
		
		void regisGsonDeserializer( Type type, JsonDeserializer< ? > adapter );
		
		void regisGsonSerializer( Type type, JsonSerializer< ? > adapter );
		
		void regisContentLoader( String entry, ContentLoader loader );
		
		< T > void regisCapability( Class< T > capability_class );
	}
	
	interface LoadContext
	{
		void regisPostLoadCallback( Consumer< PostLoadContext > callback );
		
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
	
	interface PostLoadContext
	{
		@SideOnly( Side.CLIENT )
		ItemStack defaultTabIconItem();
		
		CreativeTab defaultCreativeTab();
		
		CreativeTab hideCreativeTab();
	}
}
