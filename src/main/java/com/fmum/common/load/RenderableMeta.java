package com.fmum.common.load;

import com.google.gson.annotations.SerializedName;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Optional;

public abstract class RenderableMeta< T > extends TexturedMeta
{
	@SideOnly( Side.CLIENT )
	@SerializedName( value = "model" )
	protected String modelPath;
	
	@SideOnly( Side.CLIENT )
	protected transient T model;
	
	@Override
	@SideOnly( Side.CLIENT )
	@SuppressWarnings( "unchecked" )
	protected void checkAssetsSetup()
	{
		super.checkAssetsSetup();
		
		final String fallbackModelType = this.descriptor().name();
		this.model = ( T ) this.provider.loadModel(
			Optional.ofNullable( this.modelPath ).orElse( "" ),
			fallbackModelType,
			this::fallbackModel
		);
	}
	
	/**
	 * This will be used if failed to load required model to avoid null pointer.
	 */
	@SideOnly( Side.CLIENT )
	protected abstract T fallbackModel();
}
