package com.fmum.client.model.module;

import java.util.function.Consumer;

import com.fmum.common.util.Mesh;

public class ModelGlow extends ModelModular
{
	public static final byte
		GLOW = 0,
		NORM = 1;
	
	public ModelGlow() { }
	
	/**
	 * First input mesh will glow in the night. Rest will not.
	 */
	public ModelGlow(Mesh... meshes) { this.meshes = meshes; }
	
	public ModelGlow(Consumer<ModelGlow> initializer) { initializer.accept(this); }
	
	@Override
	public void render()
	{
		for(int i = this.meshes.length; --i > GLOW; this.meshes[i].render());
		
		glowOn();
		this.meshes[GLOW].render();
		glowOff();
	}
}
