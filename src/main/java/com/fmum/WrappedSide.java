package com.fmum;

import net.minecraftforge.fml.relauncher.Side;

/**
 * @see FMUM#SIDE
 */
public abstract class WrappedSide
{
	WrappedSide() { }
	
	/**
	 * @return Physical side of the game.
	 */
	public abstract Side getSide();
	
	public abstract void runIfClient( Runnable task );
	
//	public abstract < T > Optional< T > runIfClient( Supplier< T > task );
}
