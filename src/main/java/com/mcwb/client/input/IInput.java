package com.mcwb.client.input;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly( Side.CLIENT )
public interface IInput
{
	/**
	 * @see Key
	 */
	public String name();
	
	/**
	 * @return {@code true} if this key is currented pressed
	 */
	public boolean down();
}
