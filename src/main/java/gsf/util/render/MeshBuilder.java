package gsf.util.render;

import gsf.util.lang.Result;
import gsf.util.math.Vec3f;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.obj.OBJLoader;
import net.minecraftforge.client.model.obj.OBJModel;
import net.minecraftforge.client.model.obj.OBJModel.Normal;
import net.minecraftforge.client.model.obj.OBJModel.TextureCoordinate;
import net.minecraftforge.client.model.obj.OBJModel.Vertex;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL30;

import javax.vecmath.Vector4f;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

public class MeshBuilder
{
	protected final ArrayList< Vertex > vertices = new ArrayList<>();
	
	protected final ArrayList< Integer > indices = new ArrayList<>();
	
	
	protected MeshBuilder() { }
	
	public static Result< MeshBuilder, Exception > fromObjModel( ResourceLocation location )
	{
		return (
			Result.of( () -> OBJLoader.INSTANCE.loadModel( location ) )
			.map( OBJModel.class::cast )
			.map( MeshBuilder::from )
		);
	}
	
	public static MeshBuilder from( OBJModel model )
	{
		final MeshBuilder builder = new MeshBuilder();
		model.getMatLib().getGroups().forEach(
			( name, group ) -> group.getFaces().forEach( face -> {
				final Normal face_norm = face.getNormal();
				final Vertex[] vertices = face.getVertices();
				Arrays.stream( vertices )
					.filter( v -> !v.hasNormal() )
					.forEach( v -> v.setNormal( face_norm ) );
				builder.vertices.addAll( Arrays.asList( vertices ) );
			} )
		);
		return builder;
	}
	
	public MeshBuilder scale( float factor )
	{
		this.vertices.forEach( v -> {
			final Vector4f old_pos = v.getPos();
			final Vector4f new_pos = new Vector4f(
				old_pos.x * factor,
				old_pos.y * factor,
				old_pos.z * factor,
				old_pos.w  // * factor
			);
			v.setPos( new_pos );
		} );
		return this;
	}
	
	public MeshBuilder rebuildFaceNormal()
	{
		final Iterator< Vertex > vertex_itr = (
			this.indices.isEmpty()
			? this.vertices.iterator()
			: new Iterator< Vertex >() {
				private final Iterator< Integer > itr = MeshBuilder.this.indices.iterator();
				
				@Override
				public boolean hasNext() {
					return this.itr.hasNext();
				}
				
				@Override
				public Vertex next()
				{
					final int idx = this.itr.next();
					return MeshBuilder.this.vertices.get( idx );
				}
			}
		);
		
		final NormalCalculator calc = new NormalCalculator();
		final Vec3f norm = Vec3f.allocate();
		while ( vertex_itr.hasNext() )
		{
			final Vertex vert0 = vertex_itr.next();
			final Vertex vert1 = vertex_itr.next();
			final Vertex vert2 = vertex_itr.next();
			final Vertex vert3 = vertex_itr.next();
			
			calc.compute( vert0, vert1, vert2, norm );
			if ( norm.isOrigin() ) {
				calc.compute( vert2, vert3, vert0, norm );
				if ( norm.isOrigin() ) {
					continue;
				}
			}
			
			norm.normalize();
			final Normal new_norm = new Normal( norm );
			vert0.setNormal( new_norm );
			vert1.setNormal( new_norm );
			vert2.setNormal( new_norm );
			vert3.setNormal( new_norm );
		}
		
		Vec3f.release( norm );
		return this;
	}
	
	private static class NormalCalculator
	{
		private final Vec3f v0 = new Vec3f();
		private final Vec3f v1 = new Vec3f();
		
		private void compute( Vertex vert0, Vertex vert1, Vertex vert2, Vec3f dst )
		{
			final Vector4f p0 = vert0.getPos();
			final Vector4f p1 = vert1.getPos();
			final Vector4f p2 = vert2.getPos();
			this.getVec( p0, p1, this.v0 );
			this.getVec( p1, p2, this.v1 );
			dst.cross( this.v0, this.v1 );
		}
		
		private void getVec( Vector4f p0, Vector4f p1, Vec3f dst ) {
			dst.set( p0.x - p1.x, p0.y - p1.y, p0.z - p1.z );
		}
	}
	
	public MeshBuilder flip( boolean x, boolean y, boolean z )
	{
		this.vertices.forEach( vert -> {
			final Vector4f old_pos = vert.getPos();
			final Vector4f new_pos = new Vector4f(
				old_pos.x = x ? -old_pos.x : old_pos.x,
				old_pos.y = y ? -old_pos.y : old_pos.y,
				old_pos.z = z ? -old_pos.z : old_pos.z,
				old_pos.w
			);
			vert.setPos( new_pos );
		} );
		return this;
	}
	
	/**
	 * Will swap x and z axis of the model and flip v texture coordinate.
	 */
	public MeshBuilder toolboxObjAdapt()
	{
		this.vertices.forEach( v -> {
			final Vector4f old_pos = v.getPos();
			final Vector4f new_pos = new Vector4f( -old_pos.z, old_pos.y, old_pos.x, old_pos.w );
			v.setPos( new_pos );
			
			final TextureCoordinate old_tc = v.getTextureCoordinate();
			final TextureCoordinate new_tc = new TextureCoordinate( old_tc.u, -old_tc.v, old_tc.w );
			v.setTextureCoordinate( new_tc );
		} );
		return this;
	}
	
	public Mesh build()
	{
		final int vao = this.genVAO();
		final boolean has_indices = !this.indices.isEmpty();
		final int draw_size = ( has_indices ? this.indices : this.vertices ).size();
		assert draw_size % 4 == 0 : "The number of vertices must be a multiple of 4.";
		if ( has_indices )
		{
			return new Mesh() {
				@Override
				public void draw()
				{
					GL30.glBindVertexArray( vao );
					GL11.glDrawElements( GL11.GL_QUADS, draw_size, GL11.GL_UNSIGNED_INT, 0 );
					GL30.glBindVertexArray( 0 );
				}
				
				@Override
				public void release() {
					GL30.glDeleteVertexArrays( vao );
				}
				
				@Override
				public String toString() {
					return String.format( "%s (EBO<%d>)", super.toString(), draw_size );
				}
			};
		}
		else
		{
			return new Mesh() {
				@Override
				public void draw()
				{
					GL30.glBindVertexArray( vao );
					GL11.glDrawArrays( GL11.GL_QUADS, 0, draw_size );
					GL30.glBindVertexArray( 0 );
				}
				
				@Override
				public void release() {
					GL30.glDeleteVertexArrays( vao );
				}
				
				@Override
				public String toString() {
					return String.format( "%s (ARR<%d>)", super.toString(), draw_size );
				}
			};
		}
	}
	
	protected final int genVAO()
	{
		// Prepare vertex data data buffer.
		final FloatBuffer buffer = BufferUtils.createFloatBuffer( ( 3 + 2 + 3 ) * this.vertices.size() );
		this.vertices.forEach( vert -> {
			final Vector4f pos = vert.getPos();
			buffer.put( pos.x ).put( pos.y ).put( pos.z );
			
			final TextureCoordinate uv = vert.getTextureCoordinate();
			buffer.put( uv.u ).put( uv.v );
			
			final Normal norm = vert.getNormal();
			buffer.put( norm.x ).put( norm.y ).put( norm.z );
		} );
		buffer.flip();
		
		// Generate VAO and bind it.
		final int vao = GL30.glGenVertexArrays();
		GL30.glBindVertexArray( vao );
		
		// Buffer vertices, normals and texture coordinates.
		final int vbo = GL15.glGenBuffers();
		GL15.glBindBuffer( GL15.GL_ARRAY_BUFFER, vbo );
		GL15.glBufferData( GL15.GL_ARRAY_BUFFER, buffer, GL15.GL_STATIC_DRAW );
		
		GL11.glEnableClientState( GL11.GL_VERTEX_ARRAY );
		GL11.glEnableClientState( GL11.GL_TEXTURE_COORD_ARRAY );
		GL11.glEnableClientState( GL11.GL_NORMAL_ARRAY );
		GL11.glVertexPointer( 3, GL11.GL_FLOAT, ( 3 + 2 + 3 ) * Float.BYTES, 0 );
		GL11.glTexCoordPointer( 2, GL11.GL_FLOAT, ( 3 + 2 + 3 ) * Float.BYTES, 3 * Float.BYTES );
		GL11.glNormalPointer( GL11.GL_FLOAT, ( 3 + 2 + 3 ) * Float.BYTES, ( 3 + 2 ) * Float.BYTES );
		
		// Create EBO if it has.
		if ( !this.indices.isEmpty() )
		{
			final IntBuffer index_buffer = BufferUtils.createIntBuffer( this.indices.size() );
			this.indices.forEach( index_buffer::put );
			index_buffer.flip();
			
			final int ebo = GL15.glGenBuffers();
			GL15.glBindBuffer( GL15.GL_ELEMENT_ARRAY_BUFFER, ebo );
			GL15.glBufferData( GL15.GL_ELEMENT_ARRAY_BUFFER, index_buffer, GL15.GL_STATIC_DRAW );
			
			// Need to unbound vao first before deleting the buffer.
			GL30.glBindVertexArray( 0 );
			GL15.glDeleteBuffers( ebo );
		}
		else {
			GL30.glBindVertexArray( 0 );
		}
		
		GL15.glBindBuffer( GL15.GL_ARRAY_BUFFER, 0 );
		GL15.glDeleteBuffers( vbo );
		
		GL11.glDisableClientState( GL11.GL_VERTEX_ARRAY );
		GL11.glDisableClientState( GL11.GL_TEXTURE_COORD_ARRAY );
		GL11.glDisableClientState( GL11.GL_NORMAL_ARRAY );
		return vao;
	}
}
