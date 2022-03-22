package com.fmum.client.model;

import com.fmum.client.ResourceHandler;
import com.fmum.common.FMUM;
import com.fmum.common.util.Mesh;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
import net.minecraft.entity.Entity;

public abstract class Model extends ModelBase
{
	/**
	 * Easy referencing
	 */
	protected static final Minecraft mc = FMUM.mc;
	
	public void render(float worldScale) { }
	
	@Override
	public void render(
		Entity entityIn,
		float limbSwing,
		float limbSwingAmount,
		float ageInTicks,
		float netHeadYaw,
		float headPitch,
		float scale
	) { this.render(scale); }
	
	protected static void bindTexture(String textureLocation) {
		mc.renderEngine.bindTexture(ResourceHandler.getTexture(textureLocation));
	}
	
	/**
	 * A simple implementation of {@link Model}. It uses an array of {@link Mesh} as render targets.
	 * 
	 * @author Giant_Salted_Fish
	 */
	public static class SimpleModel extends Model
	{
		/**
		 * A simple box model
		 */
		public static final SimpleModel INSTANCE = new SimpleModel(
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
			.genNormal().build()
		);
		
		public Mesh[] meshes = Mesh.DEF_MESHES;
		
		public SimpleModel() { }
		
		public SimpleModel(Mesh mesh) { this.meshes = new Mesh[] { mesh }; }
		
		public SimpleModel(Mesh[] meshes) { this.meshes = meshes; }
		
		@Override
		public void render(float worldScale) { for(Mesh m : this.meshes) m.render(worldScale); }
	}
}
