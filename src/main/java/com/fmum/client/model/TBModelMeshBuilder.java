package com.fmum.client.model;

import com.fmum.common.FMUM;
import com.fmum.common.util.CoordSystem;
import com.fmum.common.util.Mesh;
import com.fmum.common.util.Vec3f;

/**
 * Mesh builder with native support for models exported by ToolBox in form of .java files
 * 
 * @author Giant_Salted_Fish
 */
public final class TBModelMeshBuilder extends Mesh.Builder
{
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
	 * Create a builder with proposed texture resolution set to 512x512
	 */
	public TBModelMeshBuilder() { this(512, 512); }
	
	/**
	 * Create a builder with proposed texture resolution(Usually is the texture resolution that used
	 * in building this model)
	 * 
	 * @param textureX Proposed texture resolution x
	 * @param textureY Proposed texture resolution y
	 */
	public TBModelMeshBuilder(int textureX, int textureY)
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
		CoordSystem sys = CoordSystem.pool.poll();
		Vec3f pos = Vec3f.pool.poll();

		// Note that coordinate y, z are flipped in ToolBox
		sys.setDefault();
		sys.globalRot(rotX * FMUM.TO_DEGREES, -rotY * FMUM.TO_DEGREES, rotZ * FMUM.TO_DEGREES);
		sys.trans(offX, -offY, -offZ);
		pos.set(posX, -posY, -posZ);
		
		// Setup 8 corners
		Vec3f ver0 = Vec3f.pool.poll().set(0F - x0, 0F + y0, 0F + z0);
		Vec3f ver1 = Vec3f.pool.poll().set(lenX + x1, 0F + y1, 0F + z1);
		Vec3f ver2 = Vec3f.pool.poll().set(lenX + x2, 0F + y2, -lenZ - z2);
		Vec3f ver3 = Vec3f.pool.poll().set(0F - x3, 0F + y3, -lenZ - z3);
		Vec3f ver4 = Vec3f.pool.poll().set(0F - x4, -lenY - y4, 0F + z4);
		Vec3f ver5 = Vec3f.pool.poll().set(lenX + x5, -lenY - y5, 0F + z5);
		Vec3f ver6 = Vec3f.pool.poll().set(lenX + x6, -lenY - y6, -lenZ - z6);
		Vec3f ver7 = Vec3f.pool.poll().set(0F - x7, -lenY - y7, -lenZ - z7);
		sys.apply(ver0, ver0);
		sys.apply(ver1, ver1);
		sys.apply(ver2, ver2);
		sys.apply(ver3, ver3);
		sys.apply(ver4, ver4);
		sys.apply(ver5, ver5);
		sys.apply(ver6, ver6);
		sys.apply(ver7, ver7);
		
		/// X-back ///
		this.add(pos, ver0, texU + lenZ, texV + lenZ);								// Top-right
		this.add(pos, ver4, texU + lenZ, texV + lenZ + lenY);						// Bottom-right
		this.add(pos, ver7, texU, texV + lenZ + lenY);								// Bottom-left
		this.add(pos, ver3, texU, texV + lenZ);										// Top-left
		this.addIndices(0);
		
		/// X-front ///
		this.add(pos, ver2, texU + lenZ + lenX + lenZ, texV + lenZ);				// Top-right
		this.add(pos, ver6, texU + lenZ + lenX + lenZ, texV + lenZ + lenY);			// Bottom-right
		this.add(pos, ver5, texU + lenZ + lenX, texV + lenZ + lenY);				// Bottom-left
		this.add(pos, ver1, texU + lenZ + lenX, texV + lenZ);						// Top-left
		this.addIndices(4);
		
		/// Z-back ///
		this.add(pos, ver1, texU + lenZ + lenX, texV + lenZ);						// Top-right
		this.add(pos, ver5, texU + lenZ + lenX, texV + lenZ + lenY);				// Bottom-right
		this.add(pos, ver4, texU + lenZ, texV + lenZ + lenY);						// Bottom-left
		this.add(pos, ver0, texU + lenZ, texV + lenZ);								// Top-left
		this.addIndices(8);
		
		/// Z-front ///
		this.add(pos, ver3, texU + lenZ + lenX + lenZ + lenX, texV + lenZ);			// Top-right
		this.add(pos, ver7, texU + lenZ + lenX + lenZ + lenX, texV + lenZ + lenY);	// Bottom-right
		this.add(pos, ver6, texU + lenZ + lenX + lenZ, texV + lenZ + lenY);			// Bottom-left
		this.add(pos, ver2, texU + lenZ + lenX + lenZ, texV + lenZ);				// Top-left
		this.addIndices(12);
		
		/// Y-back ///
		this.add(pos, ver0, texU + lenZ, texV + lenZ);								// Bottom-right
		this.add(pos, ver3, texU + lenZ, texV);										// Bottom-left
		this.add(pos, ver2, texU + lenZ + lenX, texV);								// Top-left
		this.add(pos, ver1, texU + lenZ + lenX, texV + lenZ);						// Top-right
		this.addIndices(16);
		
		/// Y-front ///
		this.add(pos, ver5, texU + lenZ + lenX + lenX, texV);						// Bottom-right
		this.add(pos, ver6, texU + lenZ + lenX + lenX, texV + lenZ);				// Bottom-left
		this.add(pos, ver7, texU + lenZ + lenX, texV + lenZ);						// Top-left
		this.add(pos, ver4, texU + lenZ + lenX, texV);								// Top-right
		this.addIndices(20);
		
		// Do not forget to increase index base
		this.indexBase += 24;
		
		Vec3f.pool.back(ver7);
		Vec3f.pool.back(ver6);
		Vec3f.pool.back(ver5);
		Vec3f.pool.back(ver4);
		Vec3f.pool.back(ver3);
		Vec3f.pool.back(ver2);
		Vec3f.pool.back(ver1);
		Vec3f.pool.back(ver0);
		Vec3f.pool.back(pos);
		CoordSystem.pool.back(sys);
		return this;
	}
	
	/**
	 * Another version of {@code addShapeBox(float...)} with auto adapting of the uv coordinate
	 * mapping to avoid texture distortion
	 */
	public TBModelMeshBuilder addShape(
		float posX, float posY, float posZ,
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
		
		return this;
	}
	
	public Mesh.Builder add(Vec3f pos, Vec3f offset, int u, int v)
	{
		return this.add(
			pos.x + offset.x, pos.y + offset.y, pos.z + offset.z,
			u / this.textureX, v / this.textureY
		);
	}
	
	public Mesh process() { return this.scale(PRIMARY_SCALE).genNormal().build(); }
	
	protected void addIndices(Integer index)
	{
		int base = this.indexBase + index;
		this.add(base)
			.add(base + 3)
			.add(base + 2)
			.add(base + 2)
			.add(base + 1)
			.add(base);
	}
}
