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
	
	public static final String ITEM = "item";
	
	public final BasedMotionTendency
		holdPos = new BasedMotionTendency( 0.4F, 0.125F, 0.25F ),
		holdRot = new BasedMotionTendency( 0.4F, 4.25F, 1F );
	
	public final Vec3f v0 = new Vec3f();
	
	@Override
	public void applyChannel( String channel, float smoother, Mat4f dst )
	{
		switch( channel )
		{
		case ITEM:
			final Vec3f v = this.v0;
			
			this.holdPos.getPos( v, smoother );
			dst.translate( v );
			
			this.holdRot.getPos( v, smoother );
			dst.eulerRotateYXZ( v );
			break;
			
		default:
		}
	}
}
