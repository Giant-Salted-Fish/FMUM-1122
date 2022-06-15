package com.fmum.client.model;

import com.fmum.common.util.TBModelMeshBuilder;

import net.minecraft.util.ResourceLocation;

public final class ModelAlexArm extends ModelMeshBased
{
	public static final ResourceLocation TEXTURE = new ResourceLocation("textures/entity/alex.png");
	
	public static final ModelAlexArm INSTANCE = new ModelAlexArm();
	
	public ModelAlexArm()
	{
		super(
			new TBModelMeshBuilder(64, 64)
				.addShapeBox(
					0F, 0F, 0F,
					-1.5F, -11F, -2F,
					-1.57079633F, 1.57079633F, 0F,
					3, 12, 4,
					40, 16,
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
		// TODO: do not bind texture cause player may have customized skin
		bindTexture(TEXTURE);
		super.render();
	}
}
