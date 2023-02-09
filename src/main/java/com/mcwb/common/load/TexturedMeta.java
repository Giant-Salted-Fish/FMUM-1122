package com.mcwb.common.load;

import com.google.gson.annotations.SerializedName;
import com.mcwb.common.IAutowireSideHandler;
import com.mcwb.common.meta.IMeta;
import com.mcwb.common.pack.IContentProvider;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public abstract class TexturedMeta extends BuildableMeta implements IAutowireSideHandler
{
	@SideOnly( Side.CLIENT )
	@SerializedName( value = "texture", alternate = "skin" )
	protected ResourceLocation texture;
	
	@Override
	public IMeta build( String name, IContentProvider provider )
	{
		super.build( name, provider );
		
		this.clientOnly( this::checkTextureSetup );
		return this;
	}
	
	/**
	 * Called in {@link #build(String, IContentProvider)} to ensure that texture is properly setup
	 */
	@SideOnly( Side.CLIENT )
	protected void checkTextureSetup()
	{
		if( this.texture == null )
			this.texture = this.provider.loadTexture( "textures/" + this.name + ".png" );
	}
}
