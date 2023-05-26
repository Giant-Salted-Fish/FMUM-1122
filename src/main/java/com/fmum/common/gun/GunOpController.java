package com.fmum.common.gun;

import com.fmum.client.render.IAnimator;
import com.fmum.common.player.OperationController;
import com.google.gson.annotations.SerializedName;

public class GunOpController extends OperationController
{
	@SerializedName( value = "clearGunAnimation", alternate = "noGunAnimation" )
	protected boolean clearStaticAnimation;
	
	public GunOpController() { }
	
	public GunOpController( float progressor ) { super( progressor ); }
	
	public GunOpController(
		float progressor,
		TimedEffect[] effects,
		TimedSound[] sounds,
		boolean clearStaticAnimation
	) {
		super( progressor, effects, sounds );
		
		this.clearStaticAnimation = clearStaticAnimation;
	}
	
	public IAnimator getStaticAnimation( IAnimator original ) {
		return this.clearStaticAnimation ? IAnimator.NONE : original;
	}
}
