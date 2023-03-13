package com.mcwb.client.item;

import com.mcwb.client.render.IRenderer;

import net.minecraft.util.EnumHand;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public interface IItemRenderer< C, E > extends IRenderer
{
	public static final String CHANNEL_ITEM = "item";
	
	@SideOnly( Side.CLIENT )
	public IEquippedItemRenderer< E > onTakeOut( EnumHand hand );
	
	@SideOnly( Side.CLIENT )
	public void render( C contexted );
}
