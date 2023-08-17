package com.fmum.client.input;

@FunctionalInterface
public interface IInputAdapter
{
	boolean asFlag();

	default float asValue() {
		return this.asFlag() ? 1.0F : 0.0F;
	}

//	default Vec2f asVec2() {
//		return this.asFlag() ? new Vec2f( 1.0F, 1.0F ) : Vec2f.ORIGIN;
//}
}
