package com.mcwb.common.gun;

import com.mcwb.client.render.IAnimator;
import com.mcwb.common.item.IInUseItem;
import com.mcwb.common.operation.IOperationController;
import com.mcwb.util.ArmTracker;

import net.minecraftforge.fml.relauncher.Side;

import net.minecraftforge.fml.relauncher.SideOnly;

public interface IInUseGun extends IInUseItem
{
	@Override
	public IGun< ? > using();
	
	public IOperationController loadMagController();
	
	public IOperationController unloadMagController();
	
//	@SideOnly( Side.CLIENT )
//	public float getAimProgress( float smoother );
	
	@SideOnly( Side.CLIENT )
	public void setupRenderArm( ArmTracker leftArm, ArmTracker rightArm, IAnimator animator );
}
