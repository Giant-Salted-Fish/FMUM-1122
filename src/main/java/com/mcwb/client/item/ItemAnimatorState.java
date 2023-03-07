package com.mcwb.client.item;

import com.mcwb.client.render.IAnimator;
import com.mcwb.util.DynamicPos;
import com.mcwb.util.Mat4f;
import com.mcwb.util.Quat4f;
import com.mcwb.util.Vec3f;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly( Side.CLIENT )
public class ItemAnimatorState implements IAnimator
{
	public static final ItemAnimatorState INSTANCE = new ItemAnimatorState();
	
	public final DynamicPos holdPos = new DynamicPos();
	public final DynamicPos holdRot = new DynamicPos();
	
	@Override
	public void getPos( String channel, float smoother, Vec3f dst )
	{
		switch( channel )
		{
		case IItemRenderer.CHANNEL_ITEM:
			this.holdPos.get( dst, smoother );
			break;
			
		default: dst.setZero();
		}
	}
	
	@Override
	public void getRot( String channel, float smoother, Quat4f dst )
	{
		switch( channel )
		{
		case IItemRenderer.CHANNEL_ITEM:
			final Mat4f mat = Mat4f.locate();
			final Vec3f vec = Vec3f.locate();
			
			this.holdRot.get( vec, smoother );
			mat.setIdentity();
			mat.eulerRotateYXZ( vec );
			dst.set( mat );
			
			vec.release();
			mat.release();
			break;
			
		default: dst.clearRot();
		}
	}
}
