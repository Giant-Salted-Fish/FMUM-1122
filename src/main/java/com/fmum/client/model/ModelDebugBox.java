package com.fmum.client.model;

import com.fmum.client.FMUMClient;
import com.fmum.client.ResourceManager;
import com.fmum.common.util.Mesh;
import com.fmum.common.util.ObjRepository;
import com.fmum.common.util.TBModelMeshBuilder;

import net.minecraft.util.ResourceLocation;

public final class ModelDebugBox extends ModelMeshBased implements ObjRepository<ModelDebugBox>
{
	public static final String PATH = "com.fmum.client.model.ModelDebugBox";
	
	public static final ResourceLocation TEXTURE = ResourceManager.getTexture("skins/debugbox.png");
	
	public static final ModelDebugBox INSTANCE = new ModelDebugBox(
		new TBModelMeshBuilder(32, 32)
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
	
	public ModelDebugBox() { }
	
	private ModelDebugBox(Mesh mesh) { super(mesh); }
	
	@Override
	public void render()
	{
		FMUMClient.mc.renderEngine.bindTexture(TEXTURE);
		super.render();
	}
	
	@Override
	public ModelDebugBox fetch(String identifier) { return INSTANCE; }
}
