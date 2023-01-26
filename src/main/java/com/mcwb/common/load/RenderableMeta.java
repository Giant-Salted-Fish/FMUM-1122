package com.mcwb.common.load;

import com.google.gson.annotations.SerializedName;
import com.mcwb.client.MCWBClient;
import com.mcwb.client.render.IRenderer;
import com.mcwb.common.MCWB;
import com.mcwb.common.meta.IMeta;
import com.mcwb.common.pack.IContentProvider;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public abstract class RenderableMeta< T extends IRenderer > extends TexturedMeta
{
	@SideOnly( Side.CLIENT )
	@SerializedName( value = "modelPath", alternate = "model" )
	protected String modelPath;
	
	@SideOnly( Side.CLIENT )
	protected transient T model;
	
	@Override
	@SuppressWarnings( "unchecked" )
	public IMeta build( String name, IContentProvider provider )
	{
		super.build( name, provider );
		
		// Load model if is client side
		if( MCWB.MOD.isClient() )
		{
			// Set a default model path if does not have
			final String fallbackType = this.fallbackModelType();
			if( this.modelPath == null )
				this.modelPath = "models/" + fallbackType + "/" + this.name + ".json";
			
			this.model = ( T ) MCWBClient.MOD.loadModel( this.modelPath, fallbackType, provider );
			this.modelPath = null; // TODO: if this is needed to reload the model?
		}
		return this;
	}
	
	/**
	 * <p> This will also be used as the default folder name if {@link #modelPath} is absent hence
	 * it should be lower case with no special letters. </p>
	 * 
	 * <p> In default uses the same name as its type loader {@link #loader()}. </p>
	 */
	@SideOnly( Side.CLIENT )
	protected String fallbackModelType() { return this.loader().name(); }
}
