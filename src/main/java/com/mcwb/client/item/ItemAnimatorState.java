package com.mcwb.client.item;

import com.mcwb.client.render.IAnimator;
import com.mcwb.util.DynamicPos;
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
	
	protected final Vec3f pos = new Vec3f();
	protected final Quat4f rot = new Quat4f();
	
	// TODO: make sure this is called before render
	@Override
	public void update( float smoother )
	{
		final Vec3f eulerRot = this.pos;
		this.holdRot.get( eulerRot, smoother );
		this.rot.set( eulerRot );
		
		this.holdPos.get( this.pos, smoother );
	}
	
	@Override
	public void getPos( String channel, Vec3f dst )
	{
		switch( channel )
		{
		case IItemRenderer.CHANNEL_ITEM:
			dst.set( this.pos );
			break;
			
		default: dst.setZero();
		}
	}
	
	@Override
	public void getRot( String channel, Quat4f dst )
	{
		switch( channel )
		{
		case IItemRenderer.CHANNEL_ITEM:
			dst.set( this.rot );
			break;
			
		default: dst.clearRot();
		}
	}
}
