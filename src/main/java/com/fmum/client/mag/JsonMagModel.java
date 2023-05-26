package com.fmum.client.mag;

import com.fmum.client.gun.IEquippedGunPartRenderer;
import com.fmum.client.gun.IGunPartRenderer;
import com.fmum.common.load.BuildableLoader;
import com.fmum.common.mag.IEquippedMag;
import com.fmum.common.mag.IMag;
import net.minecraft.util.EnumHand;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly( Side.CLIENT )
public class JsonMagModel extends MagModel<
	IMag< ? >,
	IEquippedMag< ? extends IMag< ? > >,
	IEquippedGunPartRenderer< ? super IEquippedMag< ? extends IMag< ? > > >,
	IGunPartRenderer<
		? super IMag< ? >,
		? extends IEquippedGunPartRenderer< ? super IEquippedMag< ? extends IMag< ? > > >
	>
> {
	public static final BuildableLoader< ? >
		LOADER = new BuildableLoader<>( "mag", JsonMagModel.class );
	
	@Override
	public IGunPartRenderer<
		? super IMag< ? >,
		? extends IEquippedGunPartRenderer< ? super IEquippedMag< ? extends IMag< ? > > >
	> newRenderer() {
		return new MagRenderer()
		{
			@Override
			public IEquippedGunPartRenderer< ? super IEquippedMag< ? extends IMag< ? > > >
				onTakeOut( EnumHand hand ) { return new EquippedGunPartRenderer(); }
		};
	}
}
