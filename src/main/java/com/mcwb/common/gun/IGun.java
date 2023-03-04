package com.mcwb.common.gun;

import javax.annotation.Nullable;

import com.mcwb.client.render.IAnimator;
import com.mcwb.util.ArmTracker;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public interface IGun< T extends IGunPart< ? extends T > > extends IGunPart< T >
{
	
	public default boolean hasMag() { return this.getInstalledCount( 0 ) > 0; }
	
	@Nullable
	public default IMag< ? > mag() {
		return this.hasMag() ? ( IMag< ? > ) this.getInstalled( 0, 0 ) : null;
	}
	
//	public default boolean isAllowed( IMag< ? > mag ) { return this.getSlot( 0 ).isAllowed( mag ); }
//	
//	public default void loadMag( IMag< ? > mag ) { mag.tryInstallTo( this, 0 ); }
//	
//	public default IMag< ? > unloadMag() { return ( IMag< ? > ) this.mag().removeFromBase( 0, 0 ); }
	
	@SideOnly( Side.CLIENT )
	public void setupRenderArm( ArmTracker leftArm, ArmTracker rightArm, IAnimator animator );
}
