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
	 * Prepare for the {@link #load()}
	 */
	public void preLoad();
	
	/**
	 * Load contents in this content provider. Called after {@link #preLoad()}.
	 */
	public void load();
	
	/**
	 * @return Author of this content provider
	 */
	public String author();
	
	/**
	 * @return
	 *     Name of source where this content provider fetches content from. Usually is the name of
	 *     the ".jar" file or the folder of the pack.
	 */
	public String sourceName();
	
	/**
	 * Register the given instance to receive {@link IPostLoadSubscriber#onPostLoad()} callback
	 */
	public default void regis( IPostLoadSubscriber subscriber ) { MCWB.MOD.regis( subscriber ); }
	
	/**
	 * <p> Register the given instance to receive {@link IMeshLoadSubscriber#onMeshLoad()} callback
	 * if the game is running on physical client side. </p>
	 * 
	 * <p> Be aware that most types that load their models via {@link #loadModel(String, String)}
	 * do not need to register for this callback as models loaded in that way will be buffered and
	 * processed automatically by {@link MCWB}. Only register for this callback if your type loads
	 * its models in other ways. </p>
	 */
	public default void regis( IMeshLoadSubscriber subscriber ) { MCWB.MOD.regis( subscriber ); }
	
	public default SoundEvent loadSound( String path ) { return MCWB.MOD.loadSound( path ); }
	
	@SideOnly( Side.CLIENT )
	public default Object loadModel( String path, String fallbackType ) {
		return MCWBClient.MOD.loadModel( path, fallbackType, this );
	}
	
	@SideOnly( Side.CLIENT )
	public default Mesh loadMesh( String path, Function< Mesh.Builder, Mesh.Builder > processor ) {
		return MCWBClient.MOD.loadMesh( path, processor );
	}
	
	@SideOnly( Side.CLIENT )
	public default ResourceLocation loadTexture( String path ) {
		return MCWBClient.MOD.loadTexture( path );
	}
	
	@SideOnly( Side.CLIENT )
	public default Animation loadAnimation( String path ) {
		return MCWBClient.MOD.loadAnimation( path );
	}
	
	public default boolean isClient() { return MCWB.MOD.isClient(); }
	
	public default void clientOnly( Runnable loadTask ) { MCWB.MOD.clientOnly( loadTask ); }
}
