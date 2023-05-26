package com.fmum.common.module;

import com.fmum.client.render.Model;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import static com.fmum.client.FMUMClient.MOD;

public interface IModifyState
{
	IModifyState NOT_SELECTED = new IModifyState() { };
	
	IModifyState SELECTED_OK = new IModifyState()
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
	
	IModifyState SELECTED_CONFLICT = new IModifyState()
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
