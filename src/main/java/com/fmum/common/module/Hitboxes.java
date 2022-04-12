package com.fmum.common.module;

import java.util.ArrayList;

import com.fmum.common.util.ConvexHitbox;
import com.fmum.common.util.CoordSystem;
import com.fmum.common.util.Vec3;

public final class Hitboxes
{
	/**
	 * Every 8 vertices forms a box. Vertex should be in order of shape box in ToolBox.
	 */
	public final ArrayList<Vec3> vertices = new ArrayList<>();
	
	public double radiusSquared = Double.MIN_VALUE;
	
	public void scale(double s)
	{
		for(Vec3 v : this.vertices)
			v.scale(s);
		this.radiusSquared *= s;
	}
	
	public ConvexHitbox[] locateHitboxes(CoordSystem pos)
	{
		ConvexHitbox[] hitboxs = new ConvexHitbox[this.vertices.size() / 8];
		for(int i = this.vertices.size(); i > 0; i -= 8)
			hitboxs[i / 8 - 1] = new ConvexHitbox(
				pos.apply(this.vertices.get(i - 1), Vec3.get()),
				pos.apply(this.vertices.get(i - 2), Vec3.get()),
				pos.apply(this.vertices.get(i - 3), Vec3.get()),
				pos.apply(this.vertices.get(i - 4), Vec3.get()),
				pos.apply(this.vertices.get(i - 5), Vec3.get()),
				pos.apply(this.vertices.get(i - 6), Vec3.get()),
				pos.apply(this.vertices.get(i - 7), Vec3.get()),
				pos.apply(this.vertices.get(i - 8), Vec3.get())
			);
		return hitboxs;
	}
	
	public Hitboxes parse(String[] split, int cursor)
	{
		while(cursor < split.length && "[".equals(split[cursor]))
		{
			for(int i = 0; ++i < 9; this.add(Vec3.parse(split[cursor + i])));
			cursor += 10;
		}
		return this;
	}
	
	private void add(Vec3 vert)
	{
		this.vertices.add(vert);
		this.radiusSquared = Math.max(this.radiusSquared, vert.lengthSquared());
	}
}
