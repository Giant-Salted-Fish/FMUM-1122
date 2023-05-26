package com.fmum.common.gun;

import com.fmum.client.module.IDeferredRenderer;
import com.fmum.client.render.IAnimator;
import com.fmum.common.item.IItem;
import com.fmum.common.module.IModule;
import com.fmum.common.paintjob.IPaintable;
import com.fmum.util.ArmTracker;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Collection;

public interface IGunPart< T extends IGunPart< ? extends T > >
	extends IItem, IModule< T >, IPaintable
{
	int leftHandPriority();
	
	int rightHandPriority();
	
	@SideOnly( Side.CLIENT )
	void prepareRenderInHandSP(
		IAnimator animator,
		Collection< IDeferredRenderer > renderQueue0,
		Collection< IDeferredRenderer > renderQueue1
	);
	
	// TODO: rename to track arm?
	@SideOnly( Side.CLIENT )
	void setupLeftArmToRender( IAnimator animator, ArmTracker leftArm );
	
	@SideOnly( Side.CLIENT )
	void setupRightArmToRender( IAnimator animator, ArmTracker rightArm );
}
