package com.fmum.common.module;

import java.util.LinkedList;

import com.fmum.common.module.TypeModular.ModuleVisitor;
import com.fmum.common.util.CoordSystem;
import com.fmum.common.util.ObjPool;
import com.fmum.common.util.Releasable;

import net.minecraft.nbt.NBTTagList;

public final class HitboxTesters extends LinkedList<HitboxTester>
	implements ModuleVisitor, Releasable
{
	private static final long serialVersionUID = 2921339352329052205L;
	
	private static final ObjPool<HitboxTesters> pool = new ObjPool<>(() -> new HitboxTesters());
	
	public int mask = 0;
	
	private HitboxTesters() { }
	
	public static HitboxTesters get(NBTTagList tag, TypeModular type)
	{
		CoordSystem sys = CoordSystem.get();
		HitboxTesters testers = get(tag, type, sys);
		sys.release();
		return testers;
	}
	
	public static HitboxTesters get(NBTTagList tag, TypeModular type, CoordSystem pos) {
		return get(tag, type, pos, TagModular.FLAG_AVOID_HITBOX_TEST);
	}
	
	public static HitboxTesters get(NBTTagList tag, TypeModular type, CoordSystem pos, int mask)
	{
		HitboxTesters testers = pool.poll();
		testers.mask = mask;
		type.stream(tag, pos, testers);
		return testers;
	}
	
	public boolean conflictWith(HitboxTesters hitboxes)
	{
		for(HitboxTester hbt0 : this)
			for(HitboxTester hbt1 : hitboxes)
				if(hbt0.conflictWith(hbt1))
					return true;
		return false;
	}
	
	@Override
	public boolean visit(NBTTagList tag, TypeModular typ, CoordSystem pos)
	{
		if((TagModular.getStates(tag)[TagModular.FLAG] & this.mask) != 0) return false;
		
		HitboxTester tester = HitboxTester.get();
		tester.type = typ;
		tester.pos.set(pos);
		this.add(tester);
		return false;
	}
	
	@Override
	public void release()
	{
		for(HitboxTester hbt : this)
			hbt.release();
		this.clear();
		pool.back(this);
	}
}
