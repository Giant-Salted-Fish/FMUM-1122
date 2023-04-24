package com.mcwb.client.gun;

import java.util.function.Supplier;

import com.mcwb.client.item.IEquippedItemRenderer;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public interface IEquippedGunPartRenderer< E > extends IEquippedItemRenderer< E >
{
	@SideOnly( Side.CLIENT )
	void useModifyAnimation( Supplier< Float > refPlayerYaw );
}
