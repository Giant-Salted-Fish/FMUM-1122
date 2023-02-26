package com.mcwb.common.gun;

import com.mcwb.client.render.IAnimator;
import com.mcwb.common.item.IItem;
import com.mcwb.common.module.IModular;
import com.mcwb.common.paintjob.IPaintable;
import com.mcwb.util.ArmTracker;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public interface IGunPart< T extends IGunPart< ? extends T > >
	extends IItem, IModular< T >, IPaintable
{
	public int leftHandPriority();
	
	public int rightHandPriority();
	
	@SideOnly( Side.CLIENT )
	public void setupLeftArmToRender( ArmTracker leftArm, IAnimator animator );
	
	@SideOnly( Side.CLIENT )
	public void setupRightArmToRender( ArmTracker rightArm, IAnimator animator );
}
