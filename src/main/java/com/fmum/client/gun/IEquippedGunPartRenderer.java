package com.fmum.client.gun;

import com.fmum.client.item.IEquippedItemRenderer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.function.Supplier;

public interface IEquippedGunPartRenderer< E > extends IEquippedItemRenderer< E >
{
	@SideOnly( Side.CLIENT )
	void useModifyAnimation( Supplier< Float > progress, Supplier< Float > refPlayerYaw );
}
