package com.fmum.client.model;

import com.fmum.client.ResourceManager;
import com.fmum.client.model.Model.SimpleModel;
import com.fmum.common.util.Mesh;

public final class ModelDebugBox extends SimpleModel
{
	public static final ModelDebugBox INSTANCE = new ModelDebugBox();
	
	public ModelDebugBox()
	{
		super(
			new Mesh.Builder()
				.add(-0.5F, -0.5F, -0.5F, 0F, 0F)
				.add(0.5F, -0.5F, -0.5F, 1F, 0F)
				.add(0.5F, 0.5F, -0.5F, 1F, 1F)
			  	.add(0.5F, 0.5F, -0.5F, 1F, 1F)
		  		.add(-0.5F, 0.5F, -0.5F, 0F, 1F)
	  			.add(-0.5F, -0.5F, -0.5F, 0F, 0F)
	  			
	  			.add(-0.5F, -0.5F, 0.5F, 0F, 0F)
	  			.add(0.5F, -0.5F, 0.5F, 1F, 0F)
	  			.add(0.5F, 0.5F, 0.5F, 1F, 1F)
	  			.add(0.5F, 0.5F, 0.5F, 1F, 1F)
	  			.add(-0.5F, 0.5F, 0.5F, 0F, 1F)
	  			.add(-0.5F, -0.5F, 0.5F, 0F, 0F)
	  			
	  			.add(-0.5F, 0.5F, 0.5F, 1F, 0F)
	  			.add(-0.5F, 0.5F, -0.5F, 1F, 1F)
	  			.add(-0.5F, -0.5F, -0.5F, 0F, 1F)
	  			.add(-0.5F, -0.5F, -0.5F, 0F, 1F)
	  			.add(-0.5F, -0.5F, 0.5F, 0F, 0F)
	  			.add(-0.5F, 0.5F, 0.5F, 1F, 0F)
	  			
	  			.add(0.5F, 0.5F, 0.5F, 1F, 0F)
	  			.add(0.5F, 0.5F, -0.5F, 1F, 1F)
	  			.add(0.5F, -0.5F, -0.5F, 0F, 1F)
	  			.add(0.5F, -0.5F, -0.5F, 0F, 1F)
	  			.add(0.5F, -0.5F, 0.5F, 0F, 0F)
	  			.add(0.5F, 0.5F, 0.5F, 1F, 0F)
	  			
	  			.add(-0.5F, -0.5F, -0.5F, 0F, 1F)
	  			.add(0.5F, -0.5F, -0.5F, 1F, 1F)
	  			.add(0.5F, -0.5F, 0.5F, 1F, 0F)
	  			.add(0.5F, -0.5F, 0.5F, 1F, 0F)
	  			.add(-0.5F, -0.5F, 0.5F, 0F, 0F)
	  			.add(-0.5F, -0.5F, -0.5F, 0F, 1F)
	  			
	  			.add(-0.5F, 0.5F, -0.5F, 0F, 1F)
	  			.add(0.5F, 0.5F, -0.5F, 1F, 1F)
	  			.add(0.5F, 0.5F, 0.5F, 1F, 0F)
	  			.add(0.5F, 0.5F, 0.5F, 1F, 0F)
	  			.add(-0.5F, 0.5F, 0.5F, 0F, 0F)
	  			.add(-0.5F, 0.5F, -0.5F, 0F, 1F)
			.scale(1F / 16F)
  			.genNormal()
  			.build()
		);
	}
	
	@Override
	public void render()
	{
		mc.renderEngine.bindTexture(ResourceManager.TEXTURE_GREEN);
		super.render();
	}
}
