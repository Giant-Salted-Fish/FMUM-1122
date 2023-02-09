package com.mcwb.common.pack;

import java.util.function.Function;

import com.mcwb.client.MCWBClient;
import com.mcwb.client.render.IRenderer;
import com.mcwb.common.MCWB;
import com.mcwb.common.load.IRequireMeshLoad;
import com.mcwb.common.load.IRequirePostLoad;
import com.mcwb.common.meta.IMeta;
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
	 * Prepare for the following {@link #load()}
	 */
	public void preLoad();
	
	/**
	 * Load contents in this content provider. Called after {@link #preLoad(MCWB)}.
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
	 * Register the given instance to receive {@link IRequirePostLoad#onPostLoad()} callback
	 */
	public default void regisPostLoad( IRequirePostLoad subscriber ) {
		MCWB.MOD.regisPostLoad( subscriber );
	}
	
	/**
	 * <p> Register the give instance to receive {@link IRequireMeshLoad#onMeshLoad()} callback if
	 * is {@link Side#CLIENT}. </p>
	 * 
	 * <p> Notice that most meta that load their models via
	 * {@link MCWBClient#loadModel(String, Class)} do not need to register for this callback as the
	 * models loaded in that way will be buffered and processed automatically by the mod. Only
	 * register your meta and process the model if its model is not loaded via that method. </p>
	 */
	public default void regisMeshLoad( IRequireMeshLoad subscriber ) {
		MCWB.MOD.regisMeshLoad( subscriber );
	}
	
	public default SoundEvent loadSound( String path ) { return MCWB.MOD.loadSound( path ); }
	
	@SideOnly( Side.CLIENT )
	public default IRenderer loadRenderer( String path, String fallBackType ) {
		return MCWBClient.MOD.loadRenderer( path, fallBackType, this );
	}
	
	@SideOnly( Side.CLIENT )
	public default Mesh loadMesh( String path, Function< Mesh.Builder, Mesh.Builder > attrSetter ) {
		return MCWBClient.MOD.loadMesh( path, attrSetter );
	}
	
	@SideOnly( Side.CLIENT )
	public default ResourceLocation loadTexture( String path ) {
		return MCWBClient.MOD.loadTexture( path );
	}
}
