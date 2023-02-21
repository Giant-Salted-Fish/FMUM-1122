package com.mcwb.client.item;

import com.mcwb.client.render.IAnimator;
import com.mcwb.util.BasedMotionTendency;
import com.mcwb.util.Mat4f;
import com.mcwb.util.Vec3f;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly( Side.CLIENT )
public class ItemAnimatorState implements IAnimator
{
	public static final ItemAnimatorState INSTANCE = new ItemAnimatorState();
	
	public final BasedMotionTendency holdPos = new BasedMotionTendency( 0.4F, 0.125F, 0.25F );
	public final BasedMotionTendency holdRot = new BasedMotionTendency( 0.4F, 4.25F, 1F );
	
	/**
	 * This can be used by {@link #applyChannel(String, float, Mat4f)} function hence you need to
	 * make sure that it is not called in between the use of this field
	 * 
	 * TODO: check and ensure this does not happen
	 */
	protected final Vec3f v0 = new Vec3f();
	
	@Override
	public void applyChannel( String channel, float smoother, Mat4f dst )
	{
		final Vec3f vec = this.v0;
		switch( channel )
		{
		case IItemRenderer.CHANNEL_ITEM:
			this.holdPos.getPos( vec, smoother );
			dst.translate( vec );
			
			this.holdRot.getPos( vec, smoother );
			dst.eulerRotateYXZ( vec );
			break;
			
		default:
		}
	}
}
