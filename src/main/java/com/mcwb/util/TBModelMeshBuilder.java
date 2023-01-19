package com.mcwb.util;

/**
 * Mesh builder with native support for models exported by ToolBox in form of .java files
 * 
 * @author Giant_Salted_Fish
 */
public class TBModelMeshBuilder extends Mesh.Builder
{
	/**
	 * Proposed texture resolution for this mesh(Usually is the texture resolution that used in
	 * building this model)
	 * 
	 * @note
	 *     This is set as {@code float} value to make sure that the final texture coordinate value
	 *     will be {@code float} value after operating with a provided {@link int} uv value
	 */
	protected final float textureX, textureY;
	
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
		final Mat4f mat = Mat4f.locate();

		// Note that coordinate y, z are flipped in ToolBox
		mat.setIdentity();
		mat.translate( posX, -posY, -posZ );
		mat.eulerRotateYXZ(
			rotX * Util.TO_DEGREES,
			rotY * Util.TO_DEGREES,
			rotZ * Util.TO_DEGREES
		);
		mat.translate( offX, -offY, -offZ );
		
		// Setup 8 corners
		final Vec3f ver0 = Vec3f.locate( 0F - x0, 0F + y0, 0F + z0 );
		final Vec3f ver1 = Vec3f.locate( lenX + x1, 0F + y1, 0F + z1 );
		final Vec3f ver2 = Vec3f.locate( lenX + x2, 0F + y2, -lenZ - z2 );
		final Vec3f ver3 = Vec3f.locate( 0F - x3, 0F + y3, -lenZ - z3 );
		final Vec3f ver4 = Vec3f.locate( 0F - x4, -lenY - y4, 0F + z4 );
		final Vec3f ver5 = Vec3f.locate( lenX + x5, -lenY - y5, 0F + z5 );
		final Vec3f ver6 = Vec3f.locate( lenX + x6, -lenY - y6, -lenZ - z6 );
		final Vec3f ver7 = Vec3f.locate( 0F - x7, -lenY - y7, -lenZ - z7 );
		mat.apply( ver0 );
		mat.apply( ver1 );
		mat.apply( ver2 );
		mat.apply( ver3 );
		mat.apply( ver4 );
		mat.apply( ver5 );
		mat.apply( ver6 );
		mat.apply( ver7 );
		
		/// X-back ///
		this.add( ver0, texU + lenZ, texV + lenZ );								// Top-right
		this.add( ver3, texU, texV + lenZ );									// Top-left
		this.add( ver7, texU, texV + lenZ + lenY );								// Bottom-left
		this.add( ver4, texU + lenZ, texV + lenZ + lenY );						// Bottom-right
		
		/// X-front ///
		this.add( ver2, texU + lenZ + lenX + lenZ, texV + lenZ );				// Top-right
		this.add( ver1, texU + lenZ + lenX, texV + lenZ );						// Top-left
		this.add( ver5, texU + lenZ + lenX, texV + lenZ + lenY );				// Bottom-left
		this.add( ver6, texU + lenZ + lenX + lenZ, texV + lenZ + lenY );		// Bottom-right
		
		/// Z-back ///
		this.add( ver1, texU + lenZ + lenX, texV + lenZ );						// Top-right
		this.add( ver0, texU + lenZ, texV + lenZ );								// Top-left
		this.add( ver4, texU + lenZ, texV + lenZ + lenY );						// Bottom-left
		this.add( ver5, texU + lenZ + lenX, texV + lenZ + lenY );				// Bottom-right
		
		/// Z-front ///
		this.add( ver3, texU + lenZ + lenX + lenZ + lenX, texV + lenZ );		// Top-right
		this.add( ver2, texU + lenZ + lenX + lenZ, texV + lenZ );				// Top-left
		this.add( ver6, texU + lenZ + lenX + lenZ, texV + lenZ + lenY );		// Bottom-left
		this.add( ver7, texU + lenZ + lenX + lenZ + lenX, texV + lenZ + lenY );	// Bottom-right
		
		/// Y-back ///
		this.add( ver0, texU + lenZ, texV + lenZ );								// Bottom-right
		this.add( ver1, texU + lenZ + lenX, texV + lenZ );						// Top-right
		this.add( ver2, texU + lenZ + lenX, texV );								// Top-left
		this.add( ver3, texU + lenZ, texV );									// Bottom-left
		
		/// Y-front ///
		this.add( ver5, texU + lenZ + lenX + lenX, texV );						// Bottom-right
		this.add( ver4, texU + lenZ + lenX, texV );								// Top-right
		this.add( ver7, texU + lenZ + lenX, texV + lenZ );						// Top-left
		this.add( ver6, texU + lenZ + lenX + lenX, texV + lenZ );				// Bottom-left
		
		ver7.release();
		ver6.release();
		ver5.release();
		ver4.release();
		ver3.release();
		ver2.release();
		ver1.release();
		ver0.release();
		mat.release();
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
		// Swap x and z axis
		final float s = Util.PRIMARY_SCALE;
		this.add(
			-vertex.z * s, vertex.y * s, vertex.x * s,
			u / this.textureX, v / this.textureY
		);
		return this;
	}
	
	/**
	 * Do general process and build the mesh. It will scale the vertices by {@value #PRIMARY_SCALE}
	 * and generate normal before the build.
	 * 
	 * @return {@code this}
	 */
	public Mesh quickBuild() { return this.genNormal().build(); }
}
