package com.mcwb.client.modify;

import com.mcwb.client.render.IAnimator;
import com.mcwb.client.render.IRenderer;
import com.mcwb.common.meta.IContexted;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

//@SideOnly( Side.CLIENT )
public interface IModifiableRenderer< T extends IContexted > extends IRenderer
{
	@SideOnly( Side.CLIENT )
	public void renderModule( T contexted, IAnimator animator );
}
