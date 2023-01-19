package com.mcwb.common.pack;

import com.mcwb.client.MCWBClient;
import com.mcwb.common.MCWB;
import com.mcwb.common.load.IRequireMeshLoad;
import com.mcwb.common.load.IRequirePostLoad;
import com.mcwb.common.meta.IMeta;

import net.minecraftforge.fml.relauncher.Side;

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
}
