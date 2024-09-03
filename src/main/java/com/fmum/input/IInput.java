package com.fmum.input;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly( Side.CLIENT )
public interface IInput
{
	boolean getAsBool();
	
	default float getAsFloat() {
		return this.getAsBool() ? 1.0F : 0.0F;
	}
	
//	default void asVec2( Vec2f dst ) {
//		Vec2f.setZero();
//	}
}
