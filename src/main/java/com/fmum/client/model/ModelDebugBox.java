package com.fmum.client.model;

import com.fmum.client.FMUMClient;
import com.fmum.common.util.ObjRepository;
import com.fmum.common.util.Mesh;

public final class ModelDebugBox extends MeshBasedModel implements ObjRepository<ModelDebugBox>
{
	public static final String PATH = "com.fmum.client.model.ModelDebugBox";
	
	public static final String TEXTURE = "skins/debugbox.png";
	
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
		.process()
	);
	
	public ModelDebugBox() { }
	
	private ModelDebugBox(Mesh mesh) { super(mesh); }
	
	@Override
	public void render()
	{
		FMUMClient.bindTexture(TEXTURE);
		super.render();
	}
	
	@Override
	public ModelDebugBox fetch(String identifier) { return INSTANCE; }
}
