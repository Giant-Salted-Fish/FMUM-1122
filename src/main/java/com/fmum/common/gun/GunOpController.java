package com.fmum.common.gun;

import com.fmum.client.render.IAnimator;
import com.fmum.common.player.OperationController;
import com.google.gson.annotations.SerializedName;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.function.Consumer;

public class GunOpController extends OperationController
{
	@SerializedName( value = "clearStaticAnimation", alternate = "noStaticAnimation" )
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
	
	@SideOnly( Side.CLIENT )
	public void setupStaticAnimation( Consumer< IAnimator > setter ) {
		if ( this.clearStaticAnimation ) { setter.accept( IAnimator.NONE ); }
	}
}
