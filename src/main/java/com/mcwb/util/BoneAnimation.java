package com.mcwb.util;

import java.util.Map.Entry;
import java.util.TreeMap;

public class BoneAnimation
{
	public static final BoneAnimation NONE = new BoneAnimation() { {
		this.mat.setIdentity();
	} };
	
	public BoneAnimation parent = NONE; // TODO
	
	public TreeMap< Float, Vec3f > pos = new TreeMap<>();
	public TreeMap< Float, Quat4f > rot = new TreeMap<>();
	
	// TODO: may not need a separate alpha channel
	public TreeMap< Float, Float > alpha = new TreeMap<>();
	
	protected final Mat4f mat = new Mat4f();
	protected final Quat4f quat = new Quat4f();
	protected float a;
	
	public void addGuard() // TODO
	{
		if( this.pos.floorKey( 0F ) == null )
			this.pos.put( 0F, Vec3f.ORIGIN );
		if( this.pos.ceilingKey( 1F ) == null )
			this.pos.put( 1F, this.pos.lowerEntry( 1F ).getValue() );
		
		if( this.rot.floorKey( 0F ) == null )
			this.rot.put( 0F, Quat4f.ORIGIN );
		if( this.rot.ceilingKey( 1F ) == null )
			this.rot.put( 1F, this.rot.lowerEntry( 1F ).getValue() );
		
		if( this.alpha.floorKey( 0F ) == null )
			this.alpha.put( 0F, 1F );
		if( this.alpha.ceilingEntry( 1F ) == null )
			this.alpha.put( 1F, this.alpha.lowerEntry( 1F ).getValue() );
	}
	
	public void update( float progress )
	{
		this.mat.set( this.parent.mat );
		this.quat.set( this.parent.quat );
		this.a = this.parent.a;
		
		/// *** Alpha *** ///
		{
			final Entry< Float, Float > floor = this.alpha.floorEntry( progress );
			final Entry< Float, Float > ceiling = this.alpha.ceilingEntry( progress );
			final float floorTime = floor.getKey();
			final float delta = ceiling.getKey() - floorTime;
			final float alpha = delta > 0F ? ( progress - floorTime ) / delta : floorTime;
			this.a *= ( 1F - alpha ) * floor.getValue() + alpha * ceiling.getValue();
		}
		
		/// *** Position *** ///
		{
			final Entry< Float, Vec3f > floor = this.pos.floorEntry( progress );
			final Entry< Float, Vec3f > ceiling = this.pos.ceilingEntry( progress );
			final float floorTime = floor.getKey();
			final float delta = ceiling.getKey() - floorTime;
			final float alpha = delta > 0F ? ( progress - floorTime ) / delta : floorTime;
			
			final Vec3f v = Vec3f.locate();
			v.set( floor.getValue() );
			v.interpolate( ceiling.getValue(), alpha );
			this.mat.translate( v );
			v.release();
		}
		
		/// *** Rotation *** ///
		{
			final Entry< Float, Quat4f > floor = this.rot.floorEntry( progress );
			final Entry< Float, Quat4f > ceiling = this.rot.ceilingEntry( progress );
			final float floorTime = floor.getKey();
			final float delta = ceiling.getKey() - floorTime;
			final float alpha = delta > 0F ? ( progress - floorTime ) / delta : floorTime;
			
			final Quat4f q = Quat4f.locate();
			q.set( floor.getValue() );
			q.interpolate( ceiling.getValue(), alpha );
			this.mat.rotate( q );
			this.quat.mul( q ); // TODO: check if this works
			q.release();
		}
		
		/// *** Scale *** ///
		{ }
	}
	
	public static class Builder
	{
		
	}
}
