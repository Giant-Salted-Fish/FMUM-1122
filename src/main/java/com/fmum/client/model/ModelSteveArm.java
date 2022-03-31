package com.fmum.client.model;

import com.fmum.common.util.TBModelMeshBuilder;

import net.minecraft.util.ResourceLocation;

public final class ModelSteveArm extends MeshBasedModel
{
	public static final ResourceLocation TEXTURE
		= new ResourceLocation("textures/entity/steve.png");
	
	public static final ModelSteveArm INSTANCE = new ModelSteveArm();
	
	public ModelSteveArm()
	{
		super(
			new TBModelMeshBuilder(64, 64)
				.addShapeBox(
					0F, 0F, 0F,
					-2F, -11F, -2F,
					-1.57079633F, 1.57079633F, 0F,
					4, 12, 4,
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
			.process()
		);
	}
}
