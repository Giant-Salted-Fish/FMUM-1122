package com.fmum.client.model;

import com.fmum.client.ResourceManager;
import com.fmum.common.util.InstanceRepository;
import com.fmum.common.util.Mesh;

public final class ModelDebugBox extends MeshBasedModel implements InstanceRepository<ModelDebugBox>
{
	public static final String PATH = "com.fmum.client.model.ModelDebugBox";
	
	public static final ModelDebugBox INSTANCE = new ModelDebugBox(
		new Mesh.Builder()
			// Z-back
			.add(-0.5F, -0.5F, -0.5F, 0F, 0F)
			.add(-0.5F, 0.5F, -0.5F, 0F, 1F)
			.add(0.5F, 0.5F, -0.5F, 1F, 1F)
			.add(0.5F, -0.5F, -0.5F, 1F, 0F)
			.add(0).add(1).add(2).add(2).add(3).add(0)
			
			// Z-front
			.add(-0.5F, -0.5F, 0.5F, 0F, 0F)
			.add(0.5F, -0.5F, 0.5F, 1F, 0F)
			.add(0.5F, 0.5F, 0.5F, 1F, 1F)
			.add(-0.5F, 0.5F, 0.5F, 0F, 1F)
			.add(4).add(5).add(6).add(6).add(7).add(4)
			
			// X-back
			.add(-0.5F, 0.5F, 0.5F, 1F, 0F)
			.add(-0.5F, 0.5F, -0.5F, 1F, 1F)
			.add(-0.5F, -0.5F, -0.5F, 0F, 1F)
			.add(-0.5F, -0.5F, 0.5F, 0F, 0F)
			.add(8).add(9).add(10).add(10).add(11).add(8)
			
			// X-front
			.add(0.5F, 0.5F, 0.5F, 1F, 0F)
			.add(0.5F, -0.5F, 0.5F, 0F, 0F)
			.add(0.5F, -0.5F, -0.5F, 0F, 1F)
			.add(0.5F, 0.5F, -0.5F, 1F, 1F)
			.add(12).add(13).add(14).add(14).add(15).add(12)
			
			// Y-back
			.add(-0.5F, -0.5F, -0.5F, 0F, 1F)
			.add(0.5F, -0.5F, -0.5F, 1F, 1F)
			.add(0.5F, -0.5F, 0.5F, 1F, 0F)
			.add(-0.5F, -0.5F, 0.5F, 0F, 0F)
			.add(16).add(17).add(18).add(18).add(19).add(16)
			
			// Y-front
			.add(-0.5F, 0.5F, -0.5F, 0F, 1F)
			.add(-0.5F, 0.5F, 0.5F, 0F, 0F)
			.add(0.5F, 0.5F, 0.5F, 1F, 0F)
			.add(0.5F, 0.5F, -0.5F, 1F, 1F)
			.add(20).add(21).add(22).add(22).add(23).add(20)
//		.scale(1F / 16F)
		.genNormal()
		.build()
	);
	
	public ModelDebugBox() { }
	
	private ModelDebugBox(Mesh mesh) { super(mesh); }
	
	@Override
	public void render()
	{
		mc.renderEngine.bindTexture(ResourceManager.TEXTURE_GREEN);
		super.render();
	}
	
	@Override
	public ModelDebugBox fetch(String identifier) { return INSTANCE; }
}
