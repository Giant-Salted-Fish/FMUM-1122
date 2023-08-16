package com.fmum.common.load;

import com.fmum.client.FMUMClient;
import net.minecraft.util.ResourceLocation;

public abstract class TexturedType extends BuildableType
{
	@SideOnly( Side.CLIENT )
	protected ResourceLocation texture;
	
	protected TexturedType() { }
	
	@SideOnly( Side.CLIENT )
	protected void _buildClientSide( IContentBuildContext ctx )
	{
		super._buildClientSide( ctx );
		
		this.texture = Optional.ofNullable( this.texture )
			.orElseGet( this::_fallbackTexture );
	}
	
	@SideOnly( Side.CLIENT )
	protected ResourceLocation _fallbackTexture( IContentBuildContext ctx ) {
		return FMUMClient.TEXTURE_GREEN;
	}
}
