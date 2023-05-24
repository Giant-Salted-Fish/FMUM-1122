package com.fmum.common.load;

import java.util.Optional;

import com.fmum.client.render.Model;
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

//		provider.clientOnly( this::checkClientSetup ); // Write like this will crash.
		provider.clientOnly( () -> this.checkAssetsSetup() );
		return this;
	}
	
	@SideOnly( Side.CLIENT )
	protected void checkAssetsSetup() {
		this.texture = Optional.ofNullable( this.texture ).orElse( Model.TEXTURE_GREEN );
	}
}
