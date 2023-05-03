package com.fmum.client.gun;

import com.fmum.client.FMUMClient;
import com.fmum.common.gun.IEquippedGun;
import com.fmum.common.gun.IGun;
import com.fmum.common.load.BuildableLoader;

import net.minecraft.util.EnumHand;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly( Side.CLIENT )
public class JsonGunModel extends GunModel<
	IGun< ? >,
	IEquippedGun< ? extends IGun< ? > >,
	IEquippedGunPartRenderer< ? super IEquippedGun< ? extends IGun< ? > > >,
	IGunRenderer<
		? super IGun< ? >,
		? extends IEquippedGunPartRenderer< ? super IEquippedGun< ? extends IGun< ? > > >
	>
> {
	public static final BuildableLoader< ? >
		LOADER = new BuildableLoader<>( "gun", JsonGunModel.class );
	
	public static final JsonGunModel NONE = new JsonGunModel();
	static { NONE.build( "", FMUMClient.MOD ); }
	
	@Override
	public IGunRenderer<
		? super IGun< ? >,
		? extends IEquippedGunPartRenderer< ? super IEquippedGun< ? extends IGun< ? > > >
	> newRenderer()
	{
		return new GunRenderer()
		{
			@Override
			public IEquippedGunPartRenderer< ? super IEquippedGun< ? extends IGun< ? > > >
				onTakeOut( EnumHand hand ) { return new EquippedGunRenderer(); }
		};
	};
}
