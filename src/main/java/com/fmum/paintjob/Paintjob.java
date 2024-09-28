package com.fmum.paintjob;

import com.fmum.FMUM;
import com.fmum.load.BuildableType;
import com.fmum.load.IContentBuildContext;
import com.fmum.load.JsonData;
import com.fmum.render.Texture;
import com.google.gson.JsonObject;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class Paintjob extends BuildableType implements IPaintjob
{
	@SideOnly( Side.CLIENT )
	protected Texture texture;
	
	@Override
	public void reload( JsonObject json, IContentBuildContext ctx )
	{
		super.reload( json, ctx );
		
		FMUM.SIDE.runIfClient( () -> {
			final JsonData data = new JsonData( json, ctx.getGson() );
			this.texture = data.get( "texture", Texture.class ).orElse( Texture.GREEN );
		} );
	}
	
	@Override
	@SideOnly( Side.CLIENT )
	public Texture getTexture() {
		return this.texture;
	}
}
