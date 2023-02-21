package com.mcwb.client.modify;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@FunctionalInterface
public interface IDeferredPriorityRenderer
	extends IDeferredRenderer, Comparable< IDeferredPriorityRenderer >
{
	@SideOnly( Side.CLIENT )
	public default void prepare() { }
	
	/**
	 * Called before first person render to determine the order of deferred render
	 */
	@SideOnly( Side.CLIENT )
	public default float priority() { return 0F; }
	
	@Override
	@SideOnly( Side.CLIENT )
	public default int compareTo( IDeferredPriorityRenderer o ) {
		return this.priority() > o.priority() ? -1 : 1;
	}
}
