package com.mcwb.client.item;

import net.minecraft.util.EnumHand;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public interface IItemRenderer< C, E >
{
	public static final String CHANNEL_ITEM = "__item__";
	
	@SideOnly( Side.CLIENT )
	public IEquippedItemRenderer< E > onTakeOut( EnumHand hand );
	
	@SideOnly( Side.CLIENT )
	public void render( C contexted );
}
