package com.mcwb.client.render;

import com.mcwb.util.Mat4f;
import com.mcwb.util.Quat4f;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * @see IRenderer
 * @author Giant_Salted_Fish
 */
@FunctionalInterface
public interface IAnimator
{
	public static final IAnimator INSTANCE = ( channel, smoother, dst ) -> { };
	
	// TODO: add update and remove smoother
	@SideOnly( Side.CLIENT )
	public void applyChannel( String channel, float smoother, Mat4f dst );
	
	// TODO: override this to get better performance
	@SideOnly( Side.CLIENT )
	public default void applyChannel( String channel, float smoother, Quat4f dst )
	{
		final Quat4f quat = Quat4f.locate();
		final Mat4f mat = Mat4f.locate();
		this.applyChannel( channel, smoother, mat );
		quat.set( mat );
		dst.mul( quat );
		mat.release();
		quat.release();
	}
	
	@SideOnly( Side.CLIENT )
	public default void getChannel( String channel, float smoother, Mat4f dst )
	{
		dst.setIdentity();
		this.applyChannel( channel, smoother, dst );
	}
	
	@SideOnly( Side.CLIENT )
	public default void getChannel( String channel, float smoother, Quat4f dst )
	{
		dst.clearRot();
		this.applyChannel( channel, smoother, dst );
	}
}
