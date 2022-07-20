package com.fmum.common.util;

import java.util.ArrayList;

import com.fmum.common.util.Mesh.Vertex;

/**
 * Mesh builder with native support for models exported by ToolBox in form of .java files
 * 
 * @author Giant_Salted_Fish
 */
public final class TBModelMeshBuilder extends Mesh.Builder
{
	public static final float TO_DEGREES = 180F / ( float ) Math.PI;
	
	public static final float PRIMARY_SCALE = 1F / 16F;
	
	/**
	 * Proposed texture resolution for this mesh(Usually is the texture resolution that used in
	 * building this model)
	 * 
	 * @note
	 *     This is set as {@code float} value to make sure that the final texture coordinate value
	 *     will be {@code float} value after operating with a provided {@link int} uv value
	 */
	public final float
		textureX,
		textureY;
	
	private int indexBase = 0;
	
	/**
	 * Create a builder with proposed texture resolution(Usually is the texture resolution that used
	 * in building this model)
	 * 
	 * @param textureX Proposed texture resolution x
	 * @param textureY Proposed texture resolution y
	 */
	public TBModelMeshBuilder( int textureX, int textureY )
	{
		this.textureX = textureX;
		this.textureY = textureY;
	}
	
	/**
	 * Corresponding to the box in ToolBox
	 * 
	 * @note Parameter offsets is applied after rotation
	 * 
	 * @param posX X-coordinate
	 * @param posY Y-coordinate
	 * @param posZ Z-coordinate
	 * @param offX X-offset
	 * @param offY Y-offset
	 * @param offZ Z-offset
	 * @param rotX X-rotate
	 * @param rotY Y-rotate
	 * @param rotZ Z-rotate
	 * @param lenX X-dimension
	 * @param lenY Y-dimension
	 * @param lenZ Z-dimension
	 * @param texU X-texture coordinate
	 * @param texV Y-texture coordinate
	 * @return {@code this}
	 */
	public TBModelMeshBuilder addBox(
		float posX, float posY, float posZ,
		float offX, float offY, float offZ,
		float rotX, float rotY, float rotZ,
		int lenX, int lenY, int lenZ,
		int texU, int texV
	) {
		return this.addShapeBox(
			posX, posY, posZ,
			offX, offY, offZ,
			rotX, rotY, rotZ,
			lenX, lenY, lenZ,
			texU, texV,
			0F, 0F, 0F,
			0F, 0F, 0F,
			0F, 0F, 0F,
			0F, 0F, 0F,
			0F, 0F, 0F,
			0F, 0F, 0F,
			0F, 0F, 0F,
			0F, 0F, 0F
		);
	}
	
	/**
	 * Corresponding to the shape box in ToolBox
	 * 
	 * @note Parameter offsets is applied after rotation
	 * 
	 * @param posX X-coordinate
	 * @param posY Y-coordinate
	 * @param posZ Z-coordinate
	 * @param offX X-offset
	 * @param offY Y-offset
	 * @param offZ Z-offset
	 * @param rotX X-rotate
	 * @param rotY Y-rotate
	 * @param rotZ Z-rotate
	 * @param lenX X-dimension
	 * @param lenY Y-dimension
	 * @param lenZ Z-dimension
	 * @param texU X-texture coordinate
	 * @param texV Y-texture coordinate
	 * @paramQueue x0,y0,z0-x7,y7,z7 Shape offsets of each vertex
	 * @return {@code this}
	 */
	public TBModelMeshBuilder addShapeBox(
		float posX, float posY, float posZ,
		float offX, float offY, float offZ,
		float rotX, float rotY, float rotZ,
		int lenX, int lenY, int lenZ,
		int texU, int texV,
		float x0, float y0, float z0,
		float x1, float y1, float z1,
		float x2, float y2, float z2,
		float x3, float y3, float z3,
		float x4, float y4, float z4,
		float x5, float y5, float z5,
		float x6, float y6, float z6,
		float x7, float y7, float z7
	) {
		CoordSystem sys = CoordSystem.locate();

		// Note that coordinate y, z are flipped in ToolBox
		sys.reset();
		sys.globalTrans( posX, -posY, -posZ );
		sys.globalRot( rotX * TO_DEGREES, rotY * TO_DEGREES, rotZ * TO_DEGREES );
		sys.trans( offX, -offY, -offZ );
		
		// Setup 8 corners
		Vec3f ver0 = Vec3f.locate( 0F - x0, 0F + y0, 0F + z0 );
		Vec3f ver1 = Vec3f.locate( lenX + x1, 0F + y1, 0F + z1 );
		Vec3f ver2 = Vec3f.locate( lenX + x2, 0F + y2, -lenZ - z2 );
		Vec3f ver3 = Vec3f.locate( 0F - x3, 0F + y3, -lenZ - z3 );
		Vec3f ver4 = Vec3f.locate( 0F - x4, -lenY - y4, 0F + z4 );
		Vec3f ver5 = Vec3f.locate( lenX + x5, -lenY - y5, 0F + z5 );
		Vec3f ver6 = Vec3f.locate( lenX + x6, -lenY - y6, -lenZ - z6 );
		Vec3f ver7 = Vec3f.locate( 0F - x7, -lenY - y7, -lenZ - z7 );
		sys.apply( ver0, ver0 );
		sys.apply( ver1, ver1 );
		sys.apply( ver2, ver2 );
		sys.apply( ver3, ver3 );
		sys.apply( ver4, ver4 );
		sys.apply( ver5, ver5 );
		sys.apply( ver6, ver6 );
		sys.apply( ver7, ver7 );
		
		/// X-back ///
		this.add( ver0, texU + lenZ, texV + lenZ );								// Top-right
		this.add( ver4, texU + lenZ, texV + lenZ + lenY );						// Bottom-right
		this.add( ver7, texU, texV + lenZ + lenY );								// Bottom-left
		this.add( ver3, texU, texV + lenZ );										// Top-left
		this.addIndices( 0 );
		
		/// X-front ///
		this.add( ver2, texU + lenZ + lenX + lenZ, texV + lenZ );					// Top-right
		this.add( ver6, texU + lenZ + lenX + lenZ, texV + lenZ + lenY );			// Bottom-right
		this.add( ver5, texU + lenZ + lenX, texV + lenZ + lenY );					// Bottom-left
		this.add( ver1, texU + lenZ + lenX, texV + lenZ );						// Top-left
		this.addIndices( 4 );
		
		/// Z-back ///
		this.add( ver1, texU + lenZ + lenX, texV + lenZ );						// Top-right
		this.add( ver5, texU + lenZ + lenX, texV + lenZ + lenY );					// Bottom-right
		this.add( ver4, texU + lenZ, texV + lenZ + lenY );						// Bottom-left
		this.add( ver0, texU + lenZ, texV + lenZ );								// Top-left
		this.addIndices( 8 );
		
		/// Z-front ///
		this.add( ver3, texU + lenZ + lenX + lenZ + lenX, texV + lenZ );			// Top-right
		this.add( ver7, texU + lenZ + lenX + lenZ + lenX, texV + lenZ + lenY );	// Bottom-right
		this.add( ver6, texU + lenZ + lenX + lenZ, texV + lenZ + lenY );			// Bottom-left
		this.add( ver2, texU + lenZ + lenX + lenZ, texV + lenZ );					// Top-left
		this.addIndices( 12 );
		
		/// Y-back ///
		this.add( ver0, texU + lenZ, texV + lenZ );								// Bottom-right
		this.add( ver3, texU + lenZ, texV );										// Bottom-left
		this.add( ver2, texU + lenZ + lenX, texV );								// Top-left
		this.add( ver1, texU + lenZ + lenX, texV + lenZ );						// Top-right
		this.addIndices( 16 );
		
		/// Y-front ///
		this.add( ver5, texU + lenZ + lenX + lenX, texV );						// Bottom-right
		this.add( ver6, texU + lenZ + lenX + lenX, texV + lenZ );					// Bottom-left
		this.add( ver7, texU + lenZ + lenX, texV + lenZ );						// Top-left
		this.add( ver4, texU + lenZ + lenX, texV );								// Top-right
		this.addIndices( 20 );
		
		// Do not forget to increase index base
		this.indexBase += 24;
		
		ver7.release();
		ver6.release();
		ver5.release();
		ver4.release();
		ver3.release();
		ver2.release();
		ver1.release();
		ver0.release();
		sys.release();
		return this;
	}
	
	/**
	 * Another version of {@code addShapeBox(float...)} with auto adapting of the uv coordinate
	 * mapping to avoid texture distortion
	 */
//	public TBModelMeshBuilder addShape(
//		float posX, float posY, float posZ,
//		float rotX, float rotY, float rotZ,
//		int lenX, int lenY, int lenZ,
//		int texU, int texV,
//		float x0, float y0, float z0,
//		float x1, float y1, float z1,
//		float x2, float y2, float z2,
//		float x3, float y3, float z3,
//		float x4, float y4, float z4,
//		float x5, float y5, float z5,
//		float x6, float y6, float z6,
//		float x7, float y7, float z7
//	) {
//		
//		return this;
//	}
	
	public TBModelMeshBuilder add( Vec3f vertex, int u, int v )
	{
		this.add(
			vertex.x, vertex.y, vertex.z,
			u / this.textureX, v / this.textureY
		);
		return this;
	}
	
	/**
	 * Generate normal for free shape quads that been added via {@code addBox(...)} and
	 * {@code addShapeBox(...)}. These quads will obtain same normal value. If a triangle of the
	 * quad is collapsed, then it is ignored. Otherwise, the final normal will be the average of the
	 * normal of two triangles.
	 * 
	 * @return {@code this}
	 */
	public final TBModelMeshBuilder genNormalForQuads()
	{
		final Vec3f
			norm = Vec3f.locate(),
			vec0 = Vec3f.locate(),
			vec1 = Vec3f.locate();
		final ArrayList<Vertex> vertices = this.vertices;
		final ArrayList<Integer> indices = this.indices;
		
		for( int i = indices.size(); ( i -= 6 ) >= 0; )
		{
			norm.set( 0F );
			
			for( int j = 6; j > 0; j -= 3 )
			{
				Vertex vert0 = vertices.get( indices.get( i + j - 3 ) );
				Vertex vert1 = vertices.get( indices.get( i + j - 2 ) );
				Vertex vert2 = vertices.get( indices.get( i + j - 1 ) );
				
				if( vert0.equals( vert1 ) || vert1.equals( vert2 ) || vert2.equals( vert0 ) )
					continue;
				
				vec0.set( vert0 ).sub( vert1 ).cross( vec1.set( vert1 ).sub( vert2 ) ).normalize();
				if( norm.nonZero() )
				{
					vec0.scale( 0.5F );
					norm.scale( 0.5F );
					norm.trans( vec0 );
				}
				else norm.set( vec0 );
			}
			
			for( int j = 6; j-- > 0; )
			{
				Vertex vert = vertices.get( indices.get( i + j ) );
				vert.normX = norm.x;
				vert.normY = norm.y;
				vert.normZ = norm.z;
			}
		}
		
		vec1.release();
		vec0.release();
		norm.release();
		return this;
	}
	
	/**
	 * Do general process and build the mesh. It will scale the vertices by {@value #PRIMARY_SCALE}
	 * and generate normal before the build.
	 * 
	 * @return {@code this}
	 */
	public Mesh quickBuild() { return this.genNormalForQuads().scale( PRIMARY_SCALE ).build(); }
	
	protected void addIndices( Integer index )
	{
		int base = this.indexBase + index;
		this.add( base )
			.add( base + 3 )
			.add( base + 2 )
			.add( base + 2 )
			.add( base + 1 )
			.add( base );
	}
}
