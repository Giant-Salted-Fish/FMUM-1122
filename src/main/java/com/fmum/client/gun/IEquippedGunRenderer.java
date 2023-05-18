package com.fmum.client.gun;

import com.fmum.client.render.IAnimation;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public interface IEquippedGunRenderer< E > extends IEquippedGunPartRenderer< E >
{
	@SideOnly( Side.CLIENT )
	void useGunAnimation( IAnimation animation );
}
