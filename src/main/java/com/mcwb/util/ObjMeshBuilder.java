package com.mcwb.util;

import java.util.Optional;

import javax.vecmath.Vector4f;

import com.mcwb.common.MCWB;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.obj.OBJLoader;
import net.minecraftforge.client.model.obj.OBJModel;

public class ObjMeshBuilder extends Mesh.Builder
{
	public ObjMeshBuilder load( String path ) throws Exception {
		return this.load( new ResourceLocation( MCWB.ID, path ) );
	}
	
	public ObjMeshBuilder load( ResourceLocation res ) throws Exception
	{
		this.load( ( OBJModel ) OBJLoader.INSTANCE.loadModel( res ) );
		return this;
	}
	
	@SuppressWarnings( "deprecation" )
	public ObjMeshBuilder load( OBJModel model )
	{
		model.getMatLib().getGroups().entrySet().forEach(
			e -> e.getValue().getFaces().forEach( face -> {
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
