package com.fmum.client.module.model;

import com.fmum.client.ResourceManager;
import com.fmum.client.model.ModelMeshBased;
import com.fmum.common.util.TBModelMeshBuilder;

public final class ModelIndicator extends ModelMeshBased
{
	public static final ModelIndicator INSTANCE = new ModelIndicator();
	
	public ModelIndicator()
	{
		super(
			new TBModelMeshBuilder(32, 32)
				.addShapeBox(-2F, -16F, -0.5F, 0F, 0F, 0F, 0F, 0F, 0F, 4, 8, 1, 1, 1, 0F, 0F, 0F, 0F, 0F, 0F, 0F, 0F, 0F, 0F, 0F, 0F, 0F, 0F, 0F, 0F, 0F, 0F, 0F, 0F, 0F, 0F, 0F, 0F)
				.addShapeBox(-4F, -8F, -0.5F, 0F, 0F, 0F, 0F, 0F, 0F, 6, 6, 1, 17, 1, 0F, 0F, 0F, 2F, 0F, 0F, 2F, 0F, 0F, 0F, 0F, 0F, -4F, 0F, 0F, -2F, 0F, 0F, -2F, 0F, 0F, -4F, 0F, 0F)
			.scale(0.1F)
			.quickBuild()
		);
	}
	
	@Override
	public void render()
	{
		bindTexture(ResourceManager.TEXTURE_GREEN);
		super.render();
	}
}
