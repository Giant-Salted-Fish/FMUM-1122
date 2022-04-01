package com.fmum.client.model;

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
	
	public ModelMeshBased(Mesh mesh) { this.meshes = new Mesh[] { mesh }; }
	
	public ModelMeshBased(Mesh[] meshes) { this.meshes = meshes; }
	
	@Override
	public void render() { for(Mesh m : this.meshes) m.render(); }
}
