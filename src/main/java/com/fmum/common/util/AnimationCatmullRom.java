package com.fmum.common.util;

import java.util.TreeSet;

public class AnimationCatmullRom extends AnimationLinear
{
	/**
	 * Buffer vector used to save intermediate states in calculation
	 */
	protected final Vec3 vec = new Vec3();
	
	public AnimationCatmullRom(double length) { super(length); }
	
	public AnimationCatmullRom(TreeSet<AnimationNode> nodes, double length) {
		super(nodes, length);
	}
	
	@Override
	public void getSmoothedPos(Vec3 dest, double smoothedProgress)
	{
		try
		{
			this.time = smoothedProgress;
			final AnimationNode p1 = this.nodes.floor(this);
			final AnimationNode p0 = this.nodes.lower(p1);
			final AnimationNode p2 = this.nodes.higher(p1);
			final AnimationNode p3 = this.nodes.higher(p2);
			
			double t = (smoothedProgress - p1.time) / (p2.time - p1.time);
			double tt = t * t;
			double ttt = tt * t;
			
			Vec3 v = this.vec;
			
			dest.set(p0.pos);
			dest.scale(-0.5D * ttt + tt - 0.5D * t);
			
			v.set(p1.pos);
			v.scale(1.5D * ttt - 2.5D * tt + 1D);
			dest.trans(v);
			
			v.set(p2.pos);
			v.scale(-1.5D * ttt + 2D * tt + 0.5D * t);
			dest.trans(v);
			
			v.set(p3.pos);
			v.scale(0.5D * ttt - 0.5D * tt);
			dest.trans(v);
		}
		catch(Exception e)
		{
			this.time = smoothedProgress;
		}
	}
	
	public static class Builder extends AnimationLinear.Builder
	{
		public Builder(double length) { super(length); }
		
		public final void createPreHeadAndPostTail()
		{
			this.nodes.add(new AnimationNode(this.nodes.first(), -1D));
			this.nodes.add(new AnimationNode(this.nodes.last(), 2D));
		}
		
		@Override
		public AnimationCatmullRom build() {
			return new AnimationCatmullRom(this.nodes, this.length);
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
