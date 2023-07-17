package com.fmum.util;

import com.fmum.common.FMUM;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.obj.OBJLoader;
import net.minecraftforge.client.model.obj.OBJModel;

import javax.vecmath.Vector4f;
import java.util.Optional;

public class ObjMeshBuilder extends Mesh.Builder
{
	public ObjMeshBuilder load( String path ) throws Exception {
		return this.load( new ResourceLocation( FMUM.MODID, path ) );
	}
	
	public ObjMeshBuilder load( ResourceLocation res ) throws Exception
	{
		this.load( ( OBJModel ) OBJLoader.INSTANCE.loadModel( res ) );
		return this;
	}
	
	public ObjMeshBuilder load( OBJModel model )
	{
		model.getMatLib().getGroups().forEach(
			( name, group ) -> group.getFaces().forEach( face -> {
				final OBJModel.Normal faceNorm = face.getNormal();
				for ( final OBJModel.Vertex vert : face.getVertices() )
				{
					final Vector4f vec = vert.getPos();
					final OBJModel.TextureCoordinate uv = vert.getTextureCoordinate();
					final OBJModel.Normal norm = Optional
						.ofNullable( vert.getNormal() ).orElse( faceNorm );
					this.add( vec.x, vec.y, vec.z, uv.u, -uv.v, norm.x, norm.y, norm.z );
				}
			} )
		);
		return this;
	}
	
	@Override
	public Mesh quickBuild() { return this.genNormal().build(); }
}
