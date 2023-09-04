package com.fmum.common.load;

import com.fmum.client.FMUMClient;
import com.fmum.client.animation.IAnimator;
import com.fmum.common.pack.IContentPackFactory.IMeshLoadContext;
import com.fmum.util.Mesh;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

// TODO: This is actually purely client only. Maybe move to client package?
@SideOnly( Side.CLIENT )
public class Model
{
	protected String path;
	
	protected float scale = 1.0F;
	
	protected boolean is_tb_model = false;
	
	protected String animation_channel = IAnimator.ABSENT_CHANNEL;
	
	protected Mesh mesh;
	
	public void loadMesh( IMeshLoadContext ctx )
	{
		try
		{
			this.mesh = ctx.loadMesh( this.path, builder -> {
				float scale = this.scale;
				if ( this.is_tb_model )
				{
					builder.swapXZ();
					scale *= 16.0F;
				}
				if ( scale != 1.0F )
				{
					builder.scale( scale );
				}
				return builder;
			} );
		}
		catch ( Exception e )
		{
			final String translation_key = "fmum.error_loading_mesh";
			FMUMClient.MOD.logException( e, translation_key, this.path );
		}
	}
	
	public void render() {
		this.mesh.render();
	}
}
