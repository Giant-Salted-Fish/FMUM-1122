package com.mcwb.common.load;

import com.google.gson.annotations.SerializedName;
import com.mcwb.common.meta.IMeta;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public abstract class RenderableMeta< T > extends TexturedMeta
{
	@SideOnly( Side.CLIENT )
	@SerializedName( value = "renderer", alternate = "model" )
	protected String rendererPath;
	
	@SideOnly( Side.CLIENT )
	protected transient T renderer;
	
	@Override
	public IMeta build( String name, IContentProvider provider )
	{
		super.build( name, provider );
		
		provider.clientOnly( this::loadRenderer );
		return this;
	}
	
	@SideOnly( Side.CLIENT )
	@SuppressWarnings( "unchecked" )
	protected void loadRenderer()
	{
		// Set a default model path if does not have
		final String fallbackType = this.loader().name();
		final String path = this.rendererPath != null ? this.rendererPath
			: "renderers/" + fallbackType + "/" + this.name + ".json";
		
		this.renderer = ( T ) this.provider.loadRenderer( path, fallbackType );
		this.rendererPath = null; // TODO: if this is needed to reload the model?
	}
}
