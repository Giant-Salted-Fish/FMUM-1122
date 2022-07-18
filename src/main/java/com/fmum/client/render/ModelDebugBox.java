package com.fmum.client.render;

import javax.annotation.Nullable;

import com.fmum.client.ResourceManager;
import com.fmum.client.item.RenderableItem;
import com.fmum.common.util.TBModelMeshBuilder;

import net.minecraft.util.ResourceLocation;

public class ModelDebugBox extends Model implements RenderableItem, ModelRepository
{
	public static final String PATH = ModelDebugBox.class.getName() + ":box";
	
	public static final ResourceLocation TEXTURE
		= ResourceManager.getTexture( "skins/debugbox.png" );
	
	public static final ModelDebugBox INSTANCE = new ModelDebugBox();
	
	private ModelDebugBox()
	{
		super(
			new TBModelMeshBuilder( 32, 32 )
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
			.quickBuild()
		);
	}
	
	@Override
	public void render()
	{
		this.bindTexture( TEXTURE );
		super.render();
	}
	
	@Override
	@Nullable
	public Renderable fetch( String identifier ) { return INSTANCE; }
}
