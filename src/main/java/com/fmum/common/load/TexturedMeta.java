package com.fmum.common.load;

import com.fmum.common.meta.IMeta;
import com.google.gson.annotations.SerializedName;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public abstract class TexturedMeta extends BuildableMeta
{
	@SideOnly( Side.CLIENT )
	@SerializedName( value = "texture", alternate = "skin" )
	protected ResourceLocation texture;
	
	@Override
	public IMeta build( String name, IContentProvider provider )
	{
		super.build( name, provider );
		
		provider.clientOnly( () -> this.checkAssetsSetup() );
//		provider.clientOnly( this::checkClientSetup ); // Write like this will crash.
		return this;
	}
	
	@SideOnly( Side.CLIENT )
	protected void checkAssetsSetup()
	{
		if ( this.texture == null ) {
			this.texture = this.provider.loadTexture( "textures/" + this.name + ".png" );
		}
	}
}
