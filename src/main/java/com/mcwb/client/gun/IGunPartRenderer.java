package com.mcwb.client.gun;

import com.mcwb.client.item.IItemRenderer;
import com.mcwb.client.modify.IModifiableRenderer;
import com.mcwb.client.render.IAnimator;
import com.mcwb.common.meta.IContexted;
import com.mcwb.util.ArmTracker;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public interface IGunPartRenderer< T extends IContexted >
	extends IItemRenderer< T >, IModifiableRenderer< T >
{
	@SideOnly( Side.CLIENT )
	public void setupLeftArmToRender( ArmTracker leftArm, IAnimator animator );
	
	@SideOnly( Side.CLIENT )
	public void setupRightArmToRender( ArmTracker rightArm, IAnimator animator );
}
