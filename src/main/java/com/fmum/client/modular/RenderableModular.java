package com.fmum.client.modular;

import java.util.ArrayList;
import java.util.Collection;

import com.fmum.client.model.Renderable;
import com.fmum.common.item.MetaItem;
import com.fmum.common.util.Releasable;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.MouseHelper;

public interface RenderableModular extends Renderable
{
	/**
	 * Render info queue
	 */
	public static final ArrayList< Releasable > infoQueue = new ArrayList<>();
	
	public default void onRenderTick( NBTTagCompound tag, MetaItem meta, MouseHelper mouse )
	{
		final Collection< Releasable > infoQueue = this.infoQueue();
		
		// Release resource located in previous render tick
		for( Releasable info : infoQueue )
			info.release();
		infoQueue.clear();
		
		// Make sure the tags are ready before rendering
//		if( meta.nbtBroken( stack ) ) return;
		
		// Position module into shoulder coordinate system
		final float smoother = this.smoother();
		// FIXME
		
		// Apply view translation if is in modification mode
//		if( this.operating() == )
		{
			
		}
//		else
		{
			// Apply animation controlled by animator
			
			// Buffer information of all modules installed on this module for future rendering
			
		}
	}
	
	public default Collection< Releasable > infoQueue() { return infoQueue; }
}
