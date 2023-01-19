package com.mcwb.common.load;

import com.google.gson.JsonDeserializer;
import com.google.gson.annotations.SerializedName;
import com.mcwb.common.MCWB;
import com.mcwb.common.meta.IMeta;
import com.mcwb.common.pack.IContentProvider;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public abstract class TexturedMeta extends BuildableMeta
{
	public static final JsonDeserializer< ResourceLocation >
		TEXTURE_ADAPTER = ( json, typeOfT, context ) -> MCWB.MOD.loadTexture( json.getAsString() );
	
	@SideOnly( Side.CLIENT )
	@SerializedName( value = "texture", alternate = "skin" )
	protected ResourceLocation texture;
	
	@Override
	public IMeta build( String name, IContentProvider provider )
	{
		super.build( name, provider );
		
		if( MCWB.MOD.isClient() )
			this.texture = this.ensureTextureSetup();
		return this;
	}
	
	/**
	 * Called in {@link #build(String, IContentProvider)} to ensure that texture is properly setup
	 */
	@SideOnly( Side.CLIENT )
	protected ResourceLocation ensureTextureSetup()
	{
		return this.texture != null ? this.texture
			: MCWB.MOD.loadTexture( "textures/" + this.name + ".png" );
	}
}
