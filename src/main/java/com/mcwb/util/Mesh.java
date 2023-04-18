package com.mcwb.util;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Nullable;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL30;

/**
 * This mesh implementation uses VAO to draw {@link GL11#GL_QUADS}.
 * 
 * @author Giant_Salted_Fish
 */
public class Mesh implements IReleasable
{
	/**
	 * A fixed instance that renders nothing.
	 */
	public static final Mesh NONE = new Mesh( 0, () -> { } )
	{
		@Override
		public String toString() { return "Mesh::NONE"; }
	};
	
	protected final int vao;
	
	protected final Runnable drawCall;
	
	protected Mesh( int vao, Runnable drawCall )
	{
		this.vao = vao;
		this.drawCall = drawCall;
	}
	
	public Mesh( List< Vertex > vertices, @Nullable List< Integer > indices )
	{
		this.vao = genVao( vertices, indices );
		if ( indices != null && indices.size() > 0 )
		{
			final int size = indices.size();
			this.drawCall = () -> GL11.glDrawElements(
				GL11.GL_QUADS, size, GL11.GL_UNSIGNED_INT, 0
			);
		}
		else
		{
			final int size = vertices.size();
			this.drawCall = () -> GL11.glDrawArrays( GL11.GL_QUADS, 0, size );
		}
	}
	
	public void render()
	{
		GL30.glBindVertexArray( this.vao );
		this.drawCall.run();
		
		// Do not forget to bind back default VAO cause mc uses this without binding to it.
		GL30.glBindVertexArray( 0 );
	}
	
	@Override
	public void release() { GL30.glDeleteVertexArrays( this.vao ); }
	
	public static int genVao( List< Vertex > vertices, @Nullable List< Integer > indices )
	{
		// Prepare vertex data data buffer.
		int count = vertices.size();
		final FloatBuffer posData = BufferUtils.createFloatBuffer( 3 * count );
		final FloatBuffer texCoordData = BufferUtils.createFloatBuffer( 2 * count );
		final FloatBuffer normData = BufferUtils.createFloatBuffer( 3 * count );
		// TODO: maybe buffer these buffers for reuse?
		
		for ( Vertex v : vertices )
		{
			posData.put( v.x );
			posData.put( v.y );
			posData.put( v.z );
			texCoordData.put( v.u );
			texCoordData.put( v.v );
			normData.put( v.normX );
			normData.put( v.normY );
			normData.put( v.normZ );
		}
		posData.flip();
		texCoordData.flip();
		normData.flip();
		
		// Generate VAO and bind it
		int vao = GL30.glGenVertexArrays();
		GL30.glBindVertexArray( vao );
		
		// Buffer vertices, normals and texture coordinates
		// FIXME: load them into one buffer. Only enable state for once
		// FIXME: seems that it is not needed for vertex pointer to work
		GL11.glEnableClientState( GL11.GL_VERTEX_ARRAY );
		int posVBO = GL15.glGenBuffers();
		GL15.glBindBuffer( GL15.GL_ARRAY_BUFFER, posVBO );
		GL15.glBufferData( GL15.GL_ARRAY_BUFFER, posData, GL15.GL_STATIC_DRAW );
		GL11.glVertexPointer( 3, GL11.GL_FLOAT, 0, 0 );
		
		GL11.glEnableClientState( GL11.GL_TEXTURE_COORD_ARRAY );
		int texCoordVBO = GL15.glGenBuffers();
		GL15.glBindBuffer( GL15.GL_ARRAY_BUFFER, texCoordVBO );
		GL15.glBufferData( GL15.GL_ARRAY_BUFFER, texCoordData, GL15.GL_STATIC_DRAW );
		GL11.glTexCoordPointer( 2, GL11.GL_FLOAT, 0, 0 );
		
		GL11.glEnableClientState( GL11.GL_NORMAL_ARRAY );
		int normVBO = GL15.glGenBuffers();
		GL15.glBindBuffer( GL15.GL_ARRAY_BUFFER, normVBO );
		GL15.glBufferData( GL15.GL_ARRAY_BUFFER, normData, GL15.GL_STATIC_DRAW );
		GL11.glNormalPointer( GL11.GL_FLOAT, 0, 0 );
		
		// Create EBO if has
		int ebo = -1;
		if ( indices != null && ( count = indices.size() ) > 0 )
		{
			final IntBuffer indexData = BufferUtils.createIntBuffer( count );
			for ( Integer i : indices ) {
				indexData.put( i );
			}
			indexData.flip();
			
			ebo = GL15.glGenBuffers();
			GL15.glBindBuffer( GL15.GL_ELEMENT_ARRAY_BUFFER, ebo );
			GL15.glBufferData( GL15.GL_ELEMENT_ARRAY_BUFFER, indexData, GL15.GL_STATIC_DRAW );
		}
		
		GL15.glBindBuffer( GL15.GL_ARRAY_BUFFER, 0 );
		GL30.glBindVertexArray( 0 );
		
		// It seems that deleting the vbos will not crash the game.
		GL15.glDeleteBuffers( posVBO );
		GL15.glDeleteBuffers( normVBO );
		GL15.glDeleteBuffers( texCoordVBO );
		if ( ebo != -1 ) GL15.glDeleteBuffers( ebo );
		
		return vao;
	}
	
	/**
	 * A C like struct used to pass vertex data.
	 * 
	 * @author Giant_Salted_Fish
	 */
	@SuppressWarnings( "serial" )
	public static final class Vertex extends Vec3f
	{
		public float u, v;
		public float normX, normY, normZ;
	}
	
	/**
	 * Add all vertices and indices of a model into this builder to build a {@link Mesh}.
	 * 
	 * @author Giant_Salted_Fish
	 */
	public static class Builder
	{
		protected final ArrayList< Vertex > vertices = new ArrayList<>();
		
		protected final ArrayList< Integer > indices = new ArrayList<>();
		
		public Builder add( float x, float y, float z, float u, float v ) {
			return this.add( x, y, z, u, v, 0, 1, 0 );
		}
		
		public Builder add(
			float x, float y, float z,
			float u, float v,
			float normX, float normY, float normZ
		) {
			final Vertex vert = new Vertex();
			vert.x = x;
			vert.y = y;
			vert.z = z;
			vert.u = u;
			vert.v = v;
			vert.normX = normX;
			vert.normY = normY;
			vert.normZ = normZ;
			return this.add( vert );
		}
		
		public Builder add( Vertex vert )
		{
			this.vertices.add( vert );
			return this;
		}
		
		public Builder add( Integer index )
		{
			this.indices.add( index );
			return this;
		}
		
		public Builder scale( float scale )
		{
			this.vertices.forEach( v -> v.scale( scale ) );
			return this;
		}
		
		public Builder genNormal()
		{
			final Iterator< Vertex > itr = (
				this.indices.size() > 0
				? new Iterator< Vertex >() {
					private final Iterator< Integer > itr = Builder.this.indices.iterator();
					
					@Override
					public boolean hasNext() { return this.itr.hasNext(); }
					
					@Override
					public Vertex next() { return Builder.this.vertices.get( this.itr.next() ); }
				}
				: this.vertices.iterator()
			);
			
			final Vec3f vec0 = Vec3f.locate();
			final Vec3f vec1 = Vec3f.locate();
			
			final Vertex[] arr = new Vertex[ 4 ];
			while ( itr.hasNext() )
			{
				for ( int i = 0; i < 4; arr[ i++ ] = itr.next() );
				for ( int j = 0; j < 4; j += 2 )
				{
					final Vertex vert0 = arr[ j ];
					final Vertex vert1 = arr[ j + 1 ];
					final Vertex vert2 = arr[ j + 2 & 3 ];
					
					vec0.set( vert0 );
					vec0.sub( vert1 );
					vec1.set( vert1 );
					vec1.sub( vert2 );
					vec0.cross( vec0, vec1 );
					
					if ( vec0.nonZero() )
					{
						vec0.normalize();
						
						for ( int k = 0; k < 4; ++k )
						{
							final Vertex v = arr[ k ];
							v.normX = vec0.x;
							v.normY = vec0.y;
							v.normZ = vec0.z;
						}
						break;
					}
				}
			}
			
			// Do not forget to release buffered vectors.
			vec1.release();
			vec0.release();
			return this;
		}
		
		public Builder flip( boolean x, boolean y, boolean z )
		{
			this.vertices.forEach( v -> {
				v.x = x ? -v.x : v.x;
				v.y = y ? -v.y : v.y;
				v.z = z ? -v.z : v.z;
			} );
			return this;
		}
		
		/**
		 * Mainly for those models that need to swap x and z coordinate. For example the .obj model
		 * exported from {@code ToolBox}.
		 */
		public Builder swapXZ()
		{
			this.vertices.forEach( v -> {
				final float oriZ = v.z;
				v.z = v.x;
				v.x = -oriZ;
			} );
			return this;
		}
		
		public Mesh build()
		{
			final int size = this.indices.size() > 0 ? this.indices.size() : this.vertices.size();
			if ( ( size % 4 ) != 0 ) // if ( ( size & 3 ) != 0 )
				throw new IllegalArgumentException(
					"Mesh currently only support quads: "
					+ this.vertices.size() + " vertices, " + this.indices.size() + " indices"
				);
			return new Mesh( this.vertices, this.indices );
		}
		
		/**
		 * Use the default recommended setting to build the mesh.
		 */
		public Mesh quickBuild() { return this.build(); }
	}
}
