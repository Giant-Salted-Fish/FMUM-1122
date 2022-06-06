package com.fmum.common.util;

import java.util.TreeSet;

public class AnimationCatmullRom extends AnimationLinear
{
	public AnimationCatmullRom( double length ) { super( length ); }
	
	public AnimationCatmullRom( TreeSet< AnimationNode > nodes, double length ) {
		super( nodes, length );
	}
	
	@Override
	public void getSmoothedPos( Vec3 dst, double smoothedProgress )
	{
		this.time = smoothedProgress;
		final AnimationNode p1 = this.nodes.floor( this );
		final AnimationNode p0 = this.nodes.lower( p1 );
		final AnimationNode p2 = this.nodes.higher( p1 );
		final AnimationNode p3 = this.nodes.higher( p2 );
		
		double t = ( smoothedProgress - p1.time ) / ( p2.time - p1.time );
		double tt = t * t;
		double ttt = tt * t;
		
		Vec3 v = this.pos;
		
		dst.set( p0.pos);
		dst.scale( -0.5D * ttt + tt - 0.5D * t );
		
		v.set( p1.pos );
		v.scale( 1.5D * ttt - 2.5D * tt + 1D );
		dst.trans( v );
		
		v.set( p2.pos );
		v.scale( -1.5D * ttt + 2D * tt + 0.5D * t );
		dst.trans( v );
		
		v.set( p3.pos );
		v.scale( 0.5D * ttt - 0.5D * tt );
		dst.trans( v );
	}
	
	public static class Builder extends AnimationLinear.Builder
	{
		public Builder( double length ) { super( length ); }
		
		public final void createPreHeadAndPostTail()
		{
			this.nodes.add( new AnimationNode( this.nodes.first(), -1D ) );
			this.nodes.add( new AnimationNode( this.nodes.last(), 2D ) );
			
			// Nodes could reach 1D which means the original last could be selected as p1
			this.nodes.add( new AnimationNode( this.nodes.last(), 3D ) );
		}
		
		@Override
		public AnimationCatmullRom build() {
			return new AnimationCatmullRom( this.nodes, this.length );
		}
		
		@Override
		public AnimationCatmullRom quickBuild()
		{
			this.createHeadAndTailOfNotExist();
			this.createPreHeadAndPostTail();
			return this.build();
		}
	}
}
