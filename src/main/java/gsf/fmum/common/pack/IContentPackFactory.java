package com.fmum.common.pack;

import com.fmum.common.load.IContentBuildContext;
import com.fmum.common.load.IContentLoader;
import com.fmum.common.load.LoaderNotFoundException;
import com.fmum.common.tab.ICreativeTab;
import com.fmum.util.Mesh;
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
import java.util.function.Function;

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
		
		void regisGsonDeserializer( Type type, JsonDeserializer< ? > adapter );
		
		void regisGsonSerializer( Type type, JsonSerializer< ? > adapter );
		
		void regisContentLoader( String entry, IContentLoader loader );
		
		< T > void regisCapability( Class< T > capability_class );
	}
	
	interface ILoadContext
	{
		void regisPostLoadCallback( Consumer< IPostLoadContext > callback );
		
		@SideOnly( Side.CLIENT )
		void regisMeshLoadCallback( Consumer< IMeshLoadContext > callback );
		
		Gson gson();
		
		default Object loadContent(
			String loader_entry,
			JsonObject object,
			IContentBuildContext ctx
		) throws LoaderNotFoundException
		{
			return this.getContentLoader( loader_entry )
				.orElseThrow( () -> new LoaderNotFoundException( loader_entry ) )
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
	
	@SideOnly( Side.CLIENT )
	interface IMeshLoadContext
	{
		Mesh loadMesh(
			String path,
			Function< Mesh.Builder, Mesh.Builder > processor
		) throws LoaderNotFoundException;
	}
}
