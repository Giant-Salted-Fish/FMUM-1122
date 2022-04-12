package com.fmum.common.module;

import java.util.ArrayList;

import com.fmum.common.util.ConvexHitbox;
import com.fmum.common.util.CoordSystem;
import com.fmum.common.util.ObjPool;
import com.fmum.common.util.Releasable;
import com.fmum.common.util.Vec3;

public final class HitboxTester implements Releasable
{
	private static final ObjPool<HitboxTester>
		pool = new ObjPool<>(() -> new HitboxTester());
	
	public CoordSystem pos = CoordSystem.get();
	
	public TypeModular type = null;
	
	/**
	 * @see TypeModular#hitbox
	 */
	public final ArrayList<ConvexHitbox[]> hitbox = new ArrayList<>(2);
	
	private HitboxTester() { }
	
	public static HitboxTester get()
	{
		HitboxTester helper = pool.poll();
		helper.hitbox.clear();
		return helper;
	}
	
	public boolean conflictWith(HitboxTester hitbox)
	{
		if(!this.checkSphereConflict(hitbox)) return false;
		for(
			int i = 0, count = Math.max(this.type.hitbox.length, hitbox.type.hitbox.length);
			i < count;
			++i
		) if(this.checkHitboxConflict(hitbox, i)) return true;
		return false;
	}
	
	@Override
	public void release() { pool.back(this); }
	
	private boolean checkSphereConflict(HitboxTester hitbox)
	{
		Vec3 v0 = this.pos.get(Vec3.get(), CoordSystem.OFFSET);
		Vec3 v1 = hitbox.pos.get(Vec3.get(), CoordSystem.OFFSET);
		
		boolean ret = (
			this.type.hitbox[0].radiusSquared + hitbox.type.hitbox[0].radiusSquared
				> v0.sub(v1).lengthSquared()
		);
		
		v0.release();
		v1.release();
		
		return ret;
	}
	
	private boolean checkHitboxConflict(HitboxTester hitbox, int index)
	{
		for(ConvexHitbox hb0 : this.getHitboxes(index))
			for(ConvexHitbox hb1 : hitbox.getHitboxes(index))
				if(hb0.conflictWith(hb1))
					return true;
		return false;
	}
	
	private ConvexHitbox[] getHitboxes(int index)
	{
		index = Math.min(this.type.hitbox.length - 1, index);
		if(this.hitbox.size() <= index)
			for(int i = this.hitbox.size(); i <= index; ++i)
				this.hitbox.add(this.type.hitbox[i].locateHitboxes(this.pos));
		return this.hitbox.get(index);
	}
}
