package com.fmum.client.gun;

import com.fmum.client.item.IItemRenderer;
import com.fmum.client.module.IDeferredRenderer;
import com.fmum.client.module.IModuleRenderer;
import com.fmum.client.render.IAnimator;
import com.fmum.util.ArmTracker;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Collection;

public interface IGunPartRenderer< C, ER > extends IItemRenderer< C, ER >, IModuleRenderer< C >
{
	@SideOnly( Side.CLIENT )
	default void prepareInHandRender(
		C contexted,
		IAnimator animator,
		Collection< IDeferredRenderer > renderQueue0,
		Collection< IDeferredRenderer > renderQueue1
	) { this.prepareRender( contexted, animator, renderQueue0, renderQueue1 ); }
	
	@SideOnly( Side.CLIENT )
	void setupLeftArmToRender( IAnimator animator, ArmTracker leftArm );
	
	@SideOnly( Side.CLIENT )
	void setupRightArmToRender( IAnimator animator, ArmTracker rightArm );
}
