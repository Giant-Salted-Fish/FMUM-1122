package com.fmum.common.gun;

import com.fmum.client.render.IAnimator;
import com.fmum.common.item.IItem;
import com.fmum.common.module.IModule;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;

public interface IGunPart< T extends IGunPart< ? extends T > >
	extends IItem, IModule< T >
{
	@SideOnly( Side.CLIENT )
	void _prepareRenderInHand( IAnimator animator, IInHandRenderContext ctx );
	
	@SideOnly( Side.CLIENT )
	interface IInHandRenderContext
	{
		void addQueuedRenderer( Runnable renderer );
		
		void addPrioritizedRenderer( IPrioritizedRenderer renderer );
	}
	
	@SideOnly( Side.CLIENT )
	interface IPrioritizedRenderer
	{
		void render();
		
		float priority();
	}
	
	class RenderQueue
	{
		// TODO: Maybe move to somewhere and hide it.
		public static final ArrayList< Runnable > NORMAL = new ArrayList<>();
		public static final ArrayList< IPrioritizedRenderer >
			PRIORITIZED = new ArrayList<>();
		
		public static final IInHandRenderContext
			IN_HAND_CONTEXT = new IInHandRenderContext()
		{
			@Override
			public void addQueuedRenderer( Runnable renderer ) {
				NORMAL.add( renderer );
			}
			
			@Override
			public void addPrioritizedRenderer( IPrioritizedRenderer renderer ) {
				PRIORITIZED.add( renderer );
			}
		};
	}
}
