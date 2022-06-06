package com.fmum.common.util;

import java.lang.ref.SoftReference;
import java.nio.Buffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL30;

/**
 * A mesh that can be rendered. Currently only support draw mode {@link GL11#GL_TRIANGLES}. Indices
 * are optional. It draws the vertices using VAO which requires minimum OpenGL version {@link GL30}.
 * It is recommended to do a capability check before really engage it into the render work.
 * 
 * @author Giant_Salted_Fish
 */
public final class Mesh implements AutoCloseable
{
	/**
	 * A fixed instance that renders nothing
	 */
	public static final Mesh NONE = new Mesh();
	
	/**
	 * A fixed instance that can be used as the initializer value
	 */
	public static final Mesh[] DEF_MESHES = { };
	
	public int vao = -1;
	
	/**
	 * No indices if this is negative
	 */
	private int drawSize = 0;
	
	private Mesh()
	{
		this.vao = 0;
		this.drawSize = 0;
	}
	
	public Mesh( List< Vertex > vertices, List< Integer > indices )
	{
		this.vao = genVAO( vertices, indices );
		this.drawSize = indices != null && indices.size() > 0 ? indices.size() : -vertices.size();
	}
	
	public void render()
	{
		GL30.glBindVertexArray( this.vao );
		if( this.drawSize > 0 )
			GL11.glDrawElements( GL11.GL_TRIANGLES, this.drawSize, GL11.GL_UNSIGNED_INT, 0 );
		else GL11.glDrawArrays( GL11.GL_TRIANGLES, 0, -this.drawSize );
		
		// Do not forget to bind back default VAO cause mc uses this without binding to it
		GL30.glBindVertexArray( 0 );
	}
	
	@Override
	public void close() { if(this.vao != -1) GL30.glDeleteVertexArrays(this.vao); }
	
	private static final BufferProducer< FloatBuffer >
		FLOAT_BUFFER_PRODUCER = count -> BufferUtils.createFloatBuffer( count );
	private static final BufferProducer< IntBuffer >
		INT_BUFFER_PRODUCER = count -> BufferUtils.createIntBuffer( count );
	synchronized public static int genVAO(
		List< Vertex > vertices,
		@Nullable List< Integer > indices
	) {
		// Prepare vertex data data buffer
		int count = vertices.size();
		FloatBuffer posData = ( FloatBuffer ) fetchBuffer( 0, 3 * count, FLOAT_BUFFER_PRODUCER );
		FloatBuffer texCoordData = ( FloatBuffer ) fetchBuffer( 1, 2 * count, FLOAT_BUFFER_PRODUCER );
		FloatBuffer normData = ( FloatBuffer ) fetchBuffer( 2, 3 * count, FLOAT_BUFFER_PRODUCER );
		
		for( Vertex v : vertices )
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
		if( indices != null && ( count = indices.size() ) > 0 )
		{
			IntBuffer indexData = ( IntBuffer ) fetchBuffer( 3, count, INT_BUFFER_PRODUCER );
			for( Integer i : indices )
				indexData.put( i );
			indexData.flip();
			
			GL15.glBindBuffer(
				GL15.GL_ELEMENT_ARRAY_BUFFER,
				ebo = GL15.glGenBuffers()
			);
			GL15.glBufferData( GL15.GL_ELEMENT_ARRAY_BUFFER, indexData, GL15.GL_STATIC_DRAW );
		}
		
		GL15.glBindBuffer( GL15.GL_ARRAY_BUFFER, 0 );
		GL30.glBindVertexArray( 0 );
		
		// FIXME: make sure deleting vbos would not crash the game
		GL15.glDeleteBuffers( posVBO );
		GL15.glDeleteBuffers( normVBO );
		GL15.glDeleteBuffers( texCoordVBO );
		if( ebo != -1 ) GL15.glDeleteBuffers( ebo );
		
		return vao;
	}

	private static final SoftReference< ? >[] bufPool = {
		new SoftReference< FloatBuffer >( null ), // Vertices
		new SoftReference< FloatBuffer >( null ), // Texture coordinates
		new SoftReference< FloatBuffer >( null ), // Normals
		new SoftReference< IntBuffer >( null )    // Indices
	};
	private static Buffer fetchBuffer(
		int index,
		int count,
		BufferProducer< ? extends Buffer > producer
	) {
		Buffer buf = ( Buffer ) bufPool[ index ].get();
		return (
			buf == null || buf.capacity() < count
			? ( Buffer ) ( bufPool[ index ] = new SoftReference<>(
				producer.produce( count )
			) ).get()
			: buf.clear()
		);
	}
	
	@FunctionalInterface
	public static interface BufferProducer< T extends Buffer > { public T produce( int count ); }
	
	/**
	 * A C like struct used to pass vertex data
	 * 
	 * @author Giant_Salted_Fish
	 */
	public static final class Vertex extends Vec3f
	{
		public float u, v;
		
		public float normX, normY, normZ;
	}
	
	/**
	 * Add all vertices and indices of a model into this builder to build a {@link Mesh}
	 * 
	 * @author Giant_Salted_Fish
	 */
	public static class Builder
	{
		public final ArrayList <Vertex > vertices = new ArrayList<>();
		
		public final ArrayList< Integer > indices = new ArrayList<>();
		
		public final Builder add( Vertex vert )
		{
			this.vertices.add( vert );
			return this;
		}
		
		public final Builder add( float x, float y, float z, float u, float v )
		{
			Vertex V = new Vertex();
			V.x = x;
			V.y = y;
			V.z = z;
			V.u = u;
			V.v = v;
			this.vertices.add( V );
			return this;
		}
		
		public final Builder add(
			float x, float y, float z,
			float u, float v,
			float normX, float normY, float normZ
		) {
			Vertex V = new Vertex();
			V.x = x;
			V.y = y;
			V.z = z;
			V.u = u;
			V.v = v;
			V.normX = normX;
			V.normY = normY;
			V.normZ = normZ;
			this.vertices.add( V );
			return this;
		}
		
		public final Builder add( Integer index )
		{
			this.indices.add( index );
			return this;
		}
		
		public final Builder scale( float scale )
		{
			for( Vertex v : this.vertices )
				v.scale( scale );
			return this;
		}
		
		public final Builder genNormal()
		{
			// Borrow two vectors for convenient operation
			final Vec3f
				vec0 = Vec3f.get(),
				vec1 = Vec3f.get();
			
			if( this.indices.size() > 0 )
				for( int i = this.indices.size(); i > 0; i -= 3 )
				{
					Vertex vert0 = this.vertices.get( this.indices.get( i - 3 ) );
					Vertex vert1 = this.vertices.get( this.indices.get( i - 2 ) );
					Vertex vert2 = this.vertices.get( this.indices.get( i - 1 ) );
					
					// Calculate normal
					vec0.set( vert0 ).sub( vert1 ).cross( vec1.set( vert1 ).sub( vert2 ) ).normalize();
					
					vert0.normX
						= vert1.normX
						= vert2.normX
						= ( float ) vec0.x;
					vert0.normY
						= vert1.normY
						= vert2.normY
						= ( float ) vec0.y;
					vert0.normZ
						= vert1.normZ
						= vert2.normZ
						= ( float ) vec0.z;
				}
			else for( int i = this.vertices.size(); i > 0; i -= 3 )
			{
				Vertex vert0 = this.vertices.get( i - 3 );
				Vertex vert1 = this.vertices.get( i - 2 );
				Vertex vert2 = this.vertices.get( i - 1 );

				vec0.set( vert0 ).sub( vert1 ).cross( vec1.set( vert1 ).sub( vert2 ) );
				
				vert0.normX
					= vert1.normX
					= vert2.normX
					= ( float ) vec0.x;
				vert0.normY
					= vert1.normY
					= vert2.normY
					= ( float ) vec0.y;
				vert0.normZ
					= vert1.normZ
					= vert2.normZ
					= ( float ) vec0.z;
			}
			
			// Do not forget to return the vectors
			vec0.release();
			vec1.release();
			
			return this;
		}
		
		public final Builder flip()
		{
			for( Vertex v : this.vertices )
				v.flip( false, true, true );
			return this;
		}
		
		public final Mesh build()
		{
			int size = this.indices.size() > 0 ? this.indices.size() : -this.vertices.size();
			if( size % 3 != 0 )
				throw new IllegalArgumentException(
					"Mesh currently only support triangles: ("
					+ ( size > 0 ? "indices)" : "vertices)" ) + size
				);
			return new Mesh( this.vertices, this.indices );
		}
		
		public Mesh quickBuild() { return this.build(); }
	}
}
