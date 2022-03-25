package com.fmum.client.model;

import com.fmum.common.util.InstanceRepository;
import com.fmum.common.util.Mesh;

public final class ModelDebugBox extends MeshBasedModel implements InstanceRepository<ModelDebugBox>
{
	public static final String PATH = "com.fmum.client.model.ModelDebugBox";
	
	public static final ModelDebugBox INSTANCE = new ModelDebugBox(
		new TBModelMeshBuilder()
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
		bindTexture("skins/debugbox.png");
		super.render();
	}
	
	@Override
	public ModelDebugBox fetch(String identifier) { return INSTANCE; }
}
