package com.fmum.input;

import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly( Side.CLIENT )
public class InputUpdateEvent extends Event
{
	public final String name;
	public final IInput input;
	
	public InputUpdateEvent( String name, IInput input )
	{
		this.name = name;
		this.input = input;
	}
}
