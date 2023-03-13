package com.mcwb.client.gun;

import java.util.Collection;

import com.mcwb.client.item.IItemRenderer;
import com.mcwb.client.module.IDeferredPriorityRenderer;
import com.mcwb.client.module.IDeferredRenderer;
import com.mcwb.client.module.IModuleRenderer;
import com.mcwb.client.render.IAnimator;
import com.mcwb.util.ArmTracker;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public interface IGunPartRenderer< C, E > extends IItemRenderer< C, E >, IModuleRenderer< C >
{
	@SideOnly( Side.CLIENT )
	public default void prepareInHandRender(
		C contexted,
		IAnimator animator,
		Collection< IDeferredRenderer > renderQueue0,
		Collection< IDeferredPriorityRenderer > renderQueue1
	) { this.prepareRender( contexted, animator, renderQueue0, renderQueue1 ); }
	
	@SideOnly( Side.CLIENT )
	public void setupLeftArmToRender( ArmTracker leftArm, IAnimator animator );
	
	@SideOnly( Side.CLIENT )
	public void setupRightArmToRender( ArmTracker rightArm, IAnimator animator );
}
