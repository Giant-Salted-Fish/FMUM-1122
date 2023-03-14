package com.mcwb.client.item;

import net.minecraft.util.EnumHand;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public interface IItemRenderer< C, ER >
{
	// TODO: these channels seems no need to expose
	public static final String CHANNEL_ITEM = "__item__";
	
	@SideOnly( Side.CLIENT )
	public ER onTakeOut( EnumHand hand );
	
	@SideOnly( Side.CLIENT )
	public void render( C contexted );
}
