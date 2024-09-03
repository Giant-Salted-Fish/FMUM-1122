package com.fmum.paintjob;

import com.fmum.FMUM;
import com.fmum.load.BuildableType;
import com.fmum.load.IContentBuildContext;
import com.fmum.render.Texture;
import com.google.gson.JsonObject;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class Paintjob extends BuildableType implements IPaintjob
{
	@SideOnly( Side.CLIENT )
	protected Texture texture;
	
	@Override
	public void build( JsonObject data, String fallback_name, IContentBuildContext ctx )
	{
		super.build( data, fallback_name, ctx );
		
		FMUM.SIDE.runIfClient( () -> {
			if ( this.texture == null ) {
				this.texture = Texture.GREEN;
			}
		} );
	}
	
	@Override
	@SideOnly( Side.CLIENT )
	public Texture getTexture() {
		return this.texture;
	}
}
