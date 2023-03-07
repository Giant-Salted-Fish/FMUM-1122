package com.mcwb.client.operation;

import javax.annotation.Nullable;

import com.mcwb.common.gun.IMag;
import com.mcwb.common.operation.IOperation;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public interface IGunOperation extends IOperation
{
	@Nullable
	@SideOnly( Side.CLIENT )
	public IMag< ? > suppliedMag();
}
