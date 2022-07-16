package com.fmum.client.item;

import com.fmum.client.render.Model;
import com.fmum.common.util.Mesh;

public class ModelItem extends Model implements RenderableItem
{
	public ModelItem() { }
	
	public ModelItem( Mesh... meshes ) { super( meshes ); }
}
