package com.fmum.client.render;

import com.fmum.client.ResourceManager;
import com.fmum.client.item.RenderableItem;
import com.fmum.common.meta.MetaBase;
import com.fmum.common.util.TBModelMeshBuilder;

import net.minecraft.util.ResourceLocation;

/**
 * A simple box model used for debug. Also is the default model that will be set if an customized
 * model can not be load.
 * 
 * @author Giant_Salted_Fish
 */
public final class ModelDebugBox extends Model implements RenderableItem
{
	public static final ModelDebugBox INSTANCE = new ModelDebugBox();
	
	private ModelDebugBox()
	{
		super( new TBModelMeshBuilder( 32, 32 )
			.addShapeBox(
				0F, 0F, 0F,
				-0.5F, -0.5F, -0.5F,
				0F, 0F, 0F,
				1, 1, 1,
				1, 1,
				0F, 0F, 0F,
				0F, 0F, 0F,
				0F, 0F, 0F,
				0F, 0F, 0F,
				0F, 0F, 0F,
				0F, 0F, 0F,
				0F, 0F, 0F,
				0F, 0F, 0F
			)
		.quickBuild() );
	}
	
	/**
	 * A default box texture exported from ToolBox 2.0
	 */
	private static final ResourceLocation TEXTURE
		= ResourceManager.getTexture( "skins/debugbox.png" );
	@Override
	public void render( MetaBase meta )
	{
		this.bindTexture( TEXTURE );
		this.render();
	}
}
