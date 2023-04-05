package com.mcwb.common.load;

import com.google.gson.annotations.SerializedName;
import com.mcwb.common.meta.IMeta;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public abstract class RenderableMeta< T > extends TexturedMeta
{
	@SideOnly( Side.CLIENT )
	@SerializedName( value = "model" )
	protected String modelPath;
	
	@SideOnly( Side.CLIENT )
	protected transient T model;
	
	@Override
	public IMeta build( String name, IContentProvider provider )
	{
		super.build( name, provider );
		
		provider.clientOnly( this::loadModel );
		return this;
	}
	
	@SideOnly( Side.CLIENT )
	@SuppressWarnings( "unchecked" )
	protected void loadModel()
	{
		// Set a default model path if does not have
		final String fallbackType = this.typer().name();
		final String path = this.modelPath != null ? this.modelPath
			: "models/" + fallbackType + "/" + this.name + ".json";
		
		this.model = ( T ) this.provider.loadModel( path, fallbackType );
		if ( this.model == null ) this.model = this.fallbackModel();
		
		this.modelPath = null; // TODO: if this is needed to reload the model?
	}
	
	/**
	 * This will be used if failed to load required model to avoid null pointer
	 */
	@SideOnly( Side.CLIENT )
	protected abstract T fallbackModel();
}
