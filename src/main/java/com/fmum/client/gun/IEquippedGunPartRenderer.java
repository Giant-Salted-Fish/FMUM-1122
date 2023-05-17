package com.fmum.client.gun;

import java.util.function.Supplier;

import com.fmum.client.item.IEquippedItemRenderer;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public interface IEquippedGunPartRenderer< E > extends IEquippedItemRenderer< E >
{
	@SideOnly( Side.CLIENT )
	void useModifyAnimation( Supplier< Float > progress, Supplier< Float > refPlayerYaw );
}
