package com.mcwb.common.gun;

import com.mcwb.common.modify.IModifiable;
import com.mcwb.util.ArmTracker;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import com.mcwb.client.render.IAnimator;
import com.mcwb.common.item.IItem;

public interface IGunPart extends IItem, IModifiable
{
	public int leftHandPriority();
	
	public int rightHandPriority();
	
	@SideOnly( Side.CLIENT )
	public void setupLeftArmToRender( ArmTracker leftArm, IAnimator animator );
	
	@SideOnly( Side.CLIENT )
	public void setupRightArmToRender( ArmTracker rightArm, IAnimator animator );
}
