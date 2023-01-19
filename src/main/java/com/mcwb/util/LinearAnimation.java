package com.mcwb.util;

import java.util.TreeSet;

public class LinearAnimation extends AnimationNode
{
	public final TreeSet<AnimationNode> nodes;
	
	/**
	 * Recommended length of the animation. Usually count in ticks.
	 */
	public float length;
	
	public LinearAnimation( float length ) { this( new TreeSet<>(), length ); }
	
	public LinearAnimation( TreeSet< AnimationNode > nodes, float length )
	{
		this.nodes = nodes;
		this.length = length;
	}
	
	@Override
	public boolean tick( float progress ) { return progress > 1F; }
	
	@Override
	public void getSmoothedPos( Vec3f dst, float smoothedProgress )
	{
		this.time = smoothedProgress;
		final AnimationNode n0 = this.nodes.floor( this );
		final AnimationNode n1 = this.nodes.higher( n0 );
		
		smoothedProgress = ( smoothedProgress - n0.time ) / ( n1.time - n0.time );
		dst.set( n1.pos );
		dst.scale( smoothedProgress );
		
		this.pos.set( n0.pos );
		this.pos.scale( 1F - smoothedProgress );
		dst.translate( this.pos );
	}
	
	public static class Builder
	{
		public final TreeSet<AnimationNode> nodes = new TreeSet<>();
		
		public float length;
		
		public Builder( float length ) { this.length = length; }
		
		public final Builder append( float x, float y, float z, float time )
		{
			this.nodes.add( new AnimationNode( x, y, z, time / this.length ) );
			return this;
		}
		
		public final Builder trans( float x, float y, float z )
		{
			for( AnimationNode n : this.nodes )
				n.pos.translate( x, y, z );
			return this;
		}
		
		public final Builder scale( float s )
		{
			for( AnimationNode n : this.nodes )
				n.pos.scale( s );
			return this;
		}
		
		public final Builder flip( boolean x, boolean y, boolean z )
		{
			for( AnimationNode n : this.nodes )
				n.pos.flip( x, y, z );
			return this;
		}
		
		public final void createHeadAndTailOfNotExist()
		{
			if( this.nodes.size() == 0 )
				this.append( 0F, 0F, 0F, 0F );
			else if( this.nodes.first().time > 0F )
				this.nodes.add( new AnimationNode( this.nodes.first(), 0F ) );
			
			if( this.nodes.last().time < 1F )
				this.nodes.add( new AnimationNode( this.nodes.last(), 1F ) );
		}
		
		public LinearAnimation build() { return new LinearAnimation( this.nodes, this.length ); }
		
		public LinearAnimation quickBuild()
		{
			this.createHeadAndTailOfNotExist();
			return this.build();
		}
	}
}
