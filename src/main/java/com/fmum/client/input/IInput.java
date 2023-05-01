package com.fmum.client.input;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly( Side.CLIENT )
public interface IInput
{
	/**
	 * @see Key
	 */
	String name();
	
	String category();
	
	/**
	 * @return {@code true} if this key is currented pressed
	 */
	boolean down();
}
