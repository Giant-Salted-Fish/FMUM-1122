package com.fmum.client.module.model;

import java.util.function.Consumer;

import com.fmum.common.util.Mesh;

public class ModelModuleGlow extends ModelModular
{
	public static final byte
		GLOW = 0,
		NORM = 1;
	
	public ModelModuleGlow() { }
	
	/**
	 * First input mesh will glow in the night. Rest will not.
	 */
	public ModelModuleGlow(Mesh... meshes) { this.meshes = meshes; }
	
	public ModelModuleGlow(Consumer<ModelModuleGlow> initializer) { initializer.accept(this); }
	
	@Override
	public void render()
	{
		for(int i = this.meshes.length; --i > GLOW; this.meshes[i].render());
		
		glowOn();
		this.meshes[GLOW].render();
		glowOff();
	}
	
	@Override
	public void renderHighLighted()
	{
		glowOn();
		super.render();
		glowOff();
	}
}
