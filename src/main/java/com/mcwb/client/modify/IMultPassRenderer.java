package com.mcwb.client.modify;

import java.util.Collection;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@FunctionalInterface
//@SideOnly( Side.CLIENT )
public interface IMultPassRenderer
{
	@SideOnly( Side.CLIENT )
	public void render( Collection< IMultPassRenderer > nextQueue );
}
