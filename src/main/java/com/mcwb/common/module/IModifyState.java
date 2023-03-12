package com.mcwb.common.module;

import com.mcwb.client.MCWBClient;
import com.mcwb.client.render.IRenderer;
import com.mcwb.client.render.Renderer;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public interface IModifyState
{
	public static final IModifyState NOT_SELECTED = new IModifyState()
	{
//		@Override
//		@SideOnly( Side.CLIENT )
//		public void doRenderArm( IRenderer renderer ) { renderer.render(); }
	};
	
	public static final IModifyState PRIMARY_START = new IModifyState() { };
	
	public static final IModifyState SELECTED_OK = new IModifyState()
	{
		@Override
		@SideOnly( Side.CLIENT )
		public void doRecommendedRender( ResourceLocation texture, IRenderer renderer )
		{
			Renderer.glowOn();
			MCWBClient.MOD.bindTexture( Renderer.TEXTURE_GREEN );
			renderer.render();
			Renderer.glowOff();
		}
	};
	
	public static final IModifyState SELECTED_CONFLICT = new IModifyState()
	{
		@Override
		@SideOnly( Side.CLIENT )
		public void doRecommendedRender( ResourceLocation texture, IRenderer renderer )
		{
			Renderer.glowOn();
			MCWBClient.MOD.bindTexture( Renderer.TEXTURE_RED );
			renderer.render();
			Renderer.glowOff();
		}
	};
	
	@SideOnly( Side.CLIENT )
	public default void doRecommendedRender( ResourceLocation texture, IRenderer renderer )
	{
		MCWBClient.MOD.bindTexture( texture );
		renderer.render();
	}
//	TODO: remove this if no longer needed
//	@SideOnly( Side.CLIENT )
//	public default void doRenderArm( IRenderer renderer ) { }
}
