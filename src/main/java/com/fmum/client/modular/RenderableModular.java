package com.fmum.client.modular;

import java.util.ArrayList;
import java.util.Collection;

import com.fmum.client.render.RenderableBase;
import com.fmum.common.module.MetaModular;
import com.fmum.common.util.Releasable;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.MouseHelper;

public interface RenderableModular extends RenderableBase
{
	/**
	 * Render info queue
	 */
	public static final ArrayList< Releasable > infoQueue = new ArrayList<>();
	
	public default void onRenderTick( NBTTagCompound tag, MetaModular meta, MouseHelper mouse )
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
		// FIXME: ton of stuff for rendering
		
		// Apply view translation if is in modification mode
		if( this.operating() == OpModify.INSTANCE )
		{
			
		}
		else
		{
			// Apply animation controlled by animator
			
			// Buffer information of all modules installed on this module for future rendering
			
		}
	}
	
	public default Collection< Releasable > infoQueue() { return infoQueue; }
}
