package com.fmum.render;

import gsf.util.animation.IPoseSetup;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@FunctionalInterface
@SideOnly( Side.CLIENT )
public interface IAnimator
{
	IAnimator NONE = channel -> IPoseSetup.EMPTY;
	
	
	IPoseSetup getChannel( String channel );
}
