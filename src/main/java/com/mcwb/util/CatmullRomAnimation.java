package com.mcwb.util;

import java.util.TreeSet;

public class CatmullRomAnimation extends LinearAnimation
{
	public CatmullRomAnimation( float length ) { super( length ); }
	
	public CatmullRomAnimation( TreeSet< AnimationNode > nodes, float length ) {
		super( nodes, length );
	}
	
	@Override
	public void getSmoothedPos( Vec3f dst, float smoothedProgress )
	{
		this.time = smoothedProgress;
		final AnimationNode p1 = this.nodes.floor( this );
		final AnimationNode p0 = this.nodes.lower( p1 );
		final AnimationNode p2 = this.nodes.higher( p1 );
		final AnimationNode p3 = this.nodes.higher( p2 );
		
		float t = ( smoothedProgress - p1.time ) / ( p2.time - p1.time );
		float tt = t * t;
		float ttt = tt * t;
		
		final Vec3f v = this.pos;
		
		dst.set( p0.pos);
		dst.scale( -0.5F * ttt + tt - 0.5F * t );
		
		v.set( p1.pos );
		v.scale( 1.5F * ttt - 2.5F * tt + 1F );
		dst.translate( v );
		
		v.set( p2.pos );
		v.scale( -1.5F * ttt + 2F * tt + 0.5F * t );
		dst.translate( v );
		
		v.set( p3.pos );
		v.scale( 0.5F * ttt - 0.5F * tt );
		dst.translate( v );
	}
	
	public static class Builder extends LinearAnimation.Builder
	{
		public Builder( float length ) { super( length ); }
		
		public final void createPreHeadAndPostTail()
		{
			this.nodes.add( new AnimationNode( this.nodes.first(), -1F ) );
			this.nodes.add( new AnimationNode( this.nodes.last(), 2F ) );
			
			// Nodes could reach 1F which means the original last could be selected as p1
			this.nodes.add( new AnimationNode( this.nodes.last(), 3F ) );
		}
		
		@Override
		public CatmullRomAnimation build() {
			return new CatmullRomAnimation( this.nodes, this.length );
		}
		
		@Override
		public CatmullRomAnimation quickBuild()
		{
			this.createHeadAndTailOfNotExist();
			this.createPreHeadAndPostTail();
			return this.build();
		}
	}
}
