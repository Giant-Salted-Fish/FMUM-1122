package com.fmum.common.util;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple pool to buffer instances like {@link Vec3f}
 *
 * @param <T> Class of buffered instance
 * 
 * @author Giant_Salted_Fish
 */
public final class ObjPool<T>
{
	private final List<T> pool;
	private final InstanceFactory<T> factory;
	private final Recycler<T> recycler;
	
	public ObjPool(List<T> pool, InstanceFactory<T> factory, Recycler<T> recycler)
	{
		this.pool = pool;
		this.factory = factory;
		this.recycler = recycler;
	}
	
	public ObjPool(InstanceFactory<T> factory)
	{
		this(
			new ArrayList<>(),
			factory,
			(pool, instance) -> { if(pool.size() < 64) pool.add(instance); }
		);
	}
	
	public T poll()
	{
		return (
			this.pool.size() > 0
			? this.pool.remove(this.pool.size() - 1)
			: this.factory.newInstance()
		);
	}
	
	public void back(T instance) { this.recycler.recycle(this.pool, instance); }
	
	@FunctionalInterface
	public static interface InstanceFactory<T> {
		public T newInstance();
	}
	
	@FunctionalInterface
	public static interface Recycler<T> {
		public void recycle(List<T> pool, T instance);
	}
}
