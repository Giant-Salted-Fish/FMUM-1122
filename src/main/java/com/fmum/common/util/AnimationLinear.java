package com.fmum.common.util;

import java.util.TreeSet;

public class AnimationLinear extends AnimationNode
{
	public final TreeSet<AnimationNode> nodes;
	
	/**
	 * Recommended length of the animation. Usually count in ticks.
	 */
	public double length;
	
	public AnimationLinear(double length) { this(new TreeSet<>(), length); }
	
	public AnimationLinear(TreeSet<AnimationNode> nodes, double length)
	{
		this.nodes = nodes;
		this.length = length;
	}
	
	@Override
	public boolean tick(double progress) { return progress > 1D; }
	
	@Override
	public void getSmoothedPos(Vec3 dest, double smoothedProgress)
	{
		// TODO
	}
	
	public static class Builder
	{
		public final TreeSet<AnimationNode> nodes = new TreeSet<>();
		
		public double length;
		
		public Builder(double length) { this.length = length; }
		
		public final Builder append(double x, double y, double z, double time)
		{
			this.nodes.add(new AnimationNode(x, y, z, time / this.length));
			return this;
		}
		
		public final Builder trans(double x, double y, double z)
		{
			for(AnimationNode n : this.nodes)
				n.pos.trans(x, y, z);
			return this;
		}
		
		public final Builder scale(double s)
		{
			for(AnimationNode n : this.nodes)
				n.pos.scale(s);
			return this;
		}
		
		public final Builder flip(boolean x, boolean y, boolean z)
		{
			for(AnimationNode n : this.nodes)
				n.pos.flip(x, y, z);
			return this;
		}
		
		public final void createHeadAndTailOfNotExist()
		{
			if(this.nodes.size() == 0)
				this.append(0D, 0D, 0D, 0D);
			else if(this.nodes.first().time > 0D)
				this.nodes.add(new AnimationNode(this.nodes.first(), 0D));
			
			if(this.nodes.last().time < 1D)
				this.nodes.add(new AnimationNode(this.nodes.last(), 1D));
		}
		
		public AnimationLinear build() { return new AnimationLinear(this.nodes, this.length); }
		
		public AnimationLinear quickBuild()
		{
			this.createHeadAndTailOfNotExist();
			return this.build();
		}
	}
}
