package com.mcwb.client.item;

import com.mcwb.client.render.IAnimator;
import com.mcwb.util.DynamicPos;
import com.mcwb.util.Mat4f;
import com.mcwb.util.Vec3f;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly( Side.CLIENT )
public class ItemAnimatorState implements IAnimator
{
	public static final ItemAnimatorState INSTANCE = new ItemAnimatorState();
	
	public final DynamicPos holdPos = new DynamicPos();
	public final DynamicPos holdRot = new DynamicPos();
	
	protected final Vec3f v0 = new Vec3f();
	
	@Override
	public void applyChannel( String channel, float smoother, Mat4f dst )
	{
		final Vec3f vec = this.v0;
		switch( channel )
		{
		case IItemRenderer.CHANNEL_ITEM:
			this.holdPos.get( vec, smoother );
			dst.translate( vec );
			
			this.holdRot.get( vec, smoother );
			dst.eulerRotateYXZ( vec );
			break;
			
		default:
		}
	}
}
