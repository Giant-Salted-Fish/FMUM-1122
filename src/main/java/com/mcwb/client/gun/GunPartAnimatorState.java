package com.mcwb.client.gun;

import com.mcwb.client.item.ModifiableItemAnimatorState;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly( Side.CLIENT )
public class GunPartAnimatorState extends ModifiableItemAnimatorState
{
	public static final GunPartAnimatorState INSTANCE = new GunPartAnimatorState();
}
