package com.fmum.common.load;

import java.util.function.Function;
import java.util.function.Supplier;

import com.fmum.client.FMUMClient;
import com.fmum.common.FMUM;
import com.fmum.common.meta.IMeta;
import com.fmum.util.Animation;
import com.fmum.util.Mesh;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Abstraction of the content provider. Usually is the content pack.
 * 
 * @author Giant_Salted_Fish
 */
public interface IContentProvider extends IMeta
{
	/**
	 * Prepare for the {@link #load()}.
	 */
	void preLoad();
	
	/**
	 * Load contents in this content provider. Called after {@link #preLoad()}.
	 */
	void load();
	
	/**
	 * @return Author of this content provider.
	 */
	String author();
	
	/**
	 * @return
	 *     Name of source where this content provider fetches content from. Usually is the name of
	 *     the ".jar" file or the folder of the pack.
	 */
	String sourceName();
	
	/**
	 * Register the given instance to receive {@link IPostLoadSubscriber#onPostLoad()} callback.
	 */
	default void regisPostLoadSubscriber( IPostLoadSubscriber subscriber ) {
		FMUM.MOD.regisPostLoadSubscriber( subscriber );
	}
	
	/**
	 * <p> Register the given instance to receive {@link IMeshLoadSubscriber#onMeshLoad()} callback
	 * if the game is running on physical client side. </p>
	 * 
	 * <p> Be aware that most types that load their models via
	 * {@link #loadModel(String, String, Supplier)} do not need to register for this callback as
	 * models loaded in that way will be buffered and processed automatically by {@link FMUM}. Only
	 * register for this callback if your type loads its models in other ways. </p>
	 */
	default void regisMeshLoadSubscriber( IMeshLoadSubscriber subscriber ) {
		FMUM.MOD.regisMeshLoadSubscriber( subscriber );
	}
	
	default SoundEvent loadSound( String path ) { return FMUM.MOD.loadSound( path ); }
	
	/**
	 * Load .json or .class model from the given path.
	 * 
	 * @param path Path of the model to load.
	 * @param fallbackModelType Type to use if "__type__" field is not defined in ".json" file.
	 * @param fallbackModel This will be used if failed to find loader or an error occurred.
	 */
	@SideOnly( Side.CLIENT )
	default Object loadModel( String path, String fallbackModelType, Supplier< ? > fallbackModel ) {
		return FMUMClient.MOD.loadModel( path, fallbackModelType, fallbackModel, this );
	}
	
	@SideOnly( Side.CLIENT )
	default Mesh loadMesh( String path, Function< Mesh.Builder, Mesh.Builder > processor ) {
		return FMUMClient.MOD.loadMesh( path, processor );
	}
	
	@SideOnly( Side.CLIENT )
	default ResourceLocation loadTexture( String path ) {
		return FMUMClient.MOD.loadTexture( path );
	}
	
	@SideOnly( Side.CLIENT )
	default Animation loadAnimation( String path ) {
		return FMUMClient.MOD.loadAnimation( path );
	}
	
	default boolean isClient() { return FMUM.MOD.isClient(); }
	
	default void clientOnly( Runnable loadTask ) { FMUM.MOD.clientOnly( loadTask ); }
}
