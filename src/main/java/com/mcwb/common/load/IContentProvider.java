package com.mcwb.common.load;

import java.util.function.Function;

import com.mcwb.client.MCWBClient;
import com.mcwb.common.MCWB;
import com.mcwb.common.meta.IMeta;
import com.mcwb.util.Animation;
import com.mcwb.util.Mesh;

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
		MCWB.MOD.regisPostLoadSubscriber( subscriber );
	}
	
	/**
	 * <p> Register the given instance to receive {@link IMeshLoadSubscriber#onMeshLoad()} callback
	 * if the game is running on physical client side. </p>
	 * 
	 * <p> Be aware that most types that load their models via {@link #loadModel(String, String)}
	 * do not need to register for this callback as models loaded in that way will be buffered and
	 * processed automatically by {@link MCWB}. Only register for this callback if your type loads
	 * its models in other ways. </p>
	 */
	default void regisMeshLoadSubscriber( IMeshLoadSubscriber subscriber ) {
		MCWB.MOD.regisMeshLoadSubscriber( subscriber );
	}
	
	default SoundEvent loadSound( String path ) { return MCWB.MOD.loadSound( path ); }
	
	@SideOnly( Side.CLIENT )
	default Object loadModel( String path, String fallbackType ) {
		return MCWBClient.MOD.loadModel( path, fallbackType, this );
	}
	
	@SideOnly( Side.CLIENT )
	default Mesh loadMesh( String path, Function< Mesh.Builder, Mesh.Builder > processor ) {
		return MCWBClient.MOD.loadMesh( path, processor );
	}
	
	@SideOnly( Side.CLIENT )
	default ResourceLocation loadTexture( String path ) {
		return MCWBClient.MOD.loadTexture( path );
	}
	
	@SideOnly( Side.CLIENT )
	default Animation loadAnimation( String path ) {
		return MCWBClient.MOD.loadAnimation( path );
	}
	
	default boolean isClient() { return MCWB.MOD.isClient(); }
	
	default void clientOnly( Runnable loadTask ) { MCWB.MOD.clientOnly( loadTask ); }
}
