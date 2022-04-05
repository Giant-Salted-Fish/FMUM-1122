package com.fmum.client.model;

import java.util.function.Consumer;

import com.fmum.common.util.Mesh;

/**
 * A simple implementation of {@link Model}. Uses an array of {@link Mesh} as render base.
 * 
 * @author Giant_Salted_Fish
 */
public class ModelMeshBased extends Model
{
	public Mesh[] meshes = Mesh.DEF_MESHES;
	
	public ModelMeshBased() { }
	
	public ModelMeshBased(Mesh... meshes) { this.meshes =meshes; }
	
	public ModelMeshBased(Consumer<ModelMeshBased> initializer) { initializer.accept(this); }
	
	@Override
	public void render() { for(Mesh m : this.meshes) m.render(); }
}
