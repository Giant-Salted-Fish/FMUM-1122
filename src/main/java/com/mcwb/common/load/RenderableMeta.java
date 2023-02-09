package com.mcwb.common.load;

import com.google.gson.annotations.SerializedName;
import com.mcwb.client.render.IRenderer;
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
	public IMeta build( String name, IContentProvider provider )
	{
		super.build( name, provider );
		
		// Load model if is client side
		this.clientOnly( this::setupRenderer );
		return this;
	}
	
	@SideOnly( Side.CLIENT )
	@SuppressWarnings( "unchecked" )
	protected void setupRenderer()
	{
		// Set a default model path if does not have
		final String fallbackType = this.loader().name();
		if( this.modelPath == null )
			this.modelPath = "models/" + fallbackType + "/" + this.name + ".json";
		
		this.model = ( T ) this.provider.loadRenderer( this.modelPath, fallbackType );
		this.modelPath = null; // TODO: if this is needed to reload the model?
	}
}
