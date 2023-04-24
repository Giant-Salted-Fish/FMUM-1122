package com.mcwb.common.module;

import static com.mcwb.client.MCWBClient.MOD;

import com.mcwb.client.render.Model;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public interface IModifyState
{
	static final IModifyState NOT_SELECTED = new IModifyState() { };
	
	static final IModifyState SELECTED_OK = new IModifyState()
	{
		@Override
		@SideOnly( Side.CLIENT )
		public void doRecommendedRender( ResourceLocation texture, Runnable renderer )
		{
			Model.glowOn();
			MOD.bindTexture( Model.TEXTURE_GREEN );
			renderer.run();
			Model.glowOff();
		}
	};
	
	static final IModifyState SELECTED_CONFLICT = new IModifyState()
	{
		@Override
		@SideOnly( Side.CLIENT )
		public void doRecommendedRender( ResourceLocation texture, Runnable renderer )
		{
			Model.glowOn();
			MOD.bindTexture( Model.TEXTURE_RED );
			renderer.run();
			Model.glowOff();
		}
	};
	
	@SideOnly( Side.CLIENT )
	default void doRecommendedRender( ResourceLocation texture, Runnable renderer )
	{
		MOD.bindTexture( texture );
		renderer.run();
	}
}
