package com.fmum.common.gun;

import com.fmum.common.module.TypeModular;
import com.fmum.common.type.TypeInfo;
import com.fmum.common.type.TypeTextParser.LocalTypeFileParser;

/**
 * Base type for all modules that can form a gun. Including gun, magazine and attachment. It
 * provides a general abstraction for all of those parts hence the frame can treat them in the same
 * way.
 * 
 * @author Giant_Salted_Fish
 */
public abstract class TypeGunPart extends TypeModular
{
	public static final LocalTypeFileParser<TypeGunPart>
		parser = new LocalTypeFileParser<>(TypeModular.parser);
	static
	{
		parser.addKeyword(
			"AimCenter",
			(s, t) -> {
				t.aimCenterY = Float.parseFloat(s[1]);
				if(s.length > 2)
					t.aimCenterZ = Float.parseFloat(s[2]);
			}
		);
	}
	
	/**
	 * Y and z coordinate in space where player can aim down with this gun part
	 */
	public float
		aimCenterY = 0F,
		aimCenterZ = 0F;
	
	protected TypeGunPart(String name) { super(name); }
	
	public int getAmmoCapacity() { return 0; }
	
	public boolean validateAmmo(TypeInfo ammo) { return false; }
	
	public boolean validateMag(TypeInfo mag) { return false; }
	
	@Override
	public void postParse()
	{
		super.postParse();
		
		// Do not forget to apply model scale
		this.aimCenterY *= this.modelScale;
		this.aimCenterZ *= this.modelScale;
	}
}
