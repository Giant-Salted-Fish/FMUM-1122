package com.fmum.common.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A simple pool to buffer instances like {@link Vec3}
 *
 * @param <T> Class of buffered instance
 * 
 * @author Giant_Salted_Fish
 */
public final class ObjPool< T >
{
	private final List< T > pool;
	private final ObjFactory< T > factory;
	private final Recycler< T > recycler;
	
	/**
	 * In default it uses a synchronized {@link ArrayList} as pool which guarantee the thread safe
	 * 
	 * @param factory Instance factory which provides instance when there is none left in pool
	 */
	public ObjPool( ObjFactory< T > factory )
	{
		this(
			factory,
			( instance, pool ) -> { if( pool.size() < 64 ) pool.add( instance ); }
		);
	}
	
	public ObjPool( ObjFactory< T > factory, Recycler< T > recycler )
	{
		this(
			Collections.synchronizedList( new ArrayList<>() ),
			factory,
			recycler
		);
	}
	
	public ObjPool( List< T > pool, ObjFactory< T > factory, Recycler< T > recycler )
	{
		this.pool = pool;
		this.factory = factory;
		this.recycler = recycler;
	}
	
	public T poll()
	{
		return (
			this.pool.size() > 0
			? this.pool.remove( this.pool.size() - 1 )
			: this.factory.produce()
		);
	}
	
	public void back( T instance ) { this.recycler.recycle( instance, this.pool ); }
	
	@FunctionalInterface
	public static interface Recycler< T > { public void recycle( T instance, List< T > pool ); }
}