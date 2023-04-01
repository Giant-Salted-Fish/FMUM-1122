package com.mcwb.client.gun;

import com.mcwb.client.MCWBClient;
import com.mcwb.client.item.IEquippedItemRenderer;
import com.mcwb.common.gun.IEquippedGun;
import com.mcwb.common.gun.IGun;
import com.mcwb.common.load.BuildableLoader;

import net.minecraft.util.EnumHand;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly( Side.CLIENT )
public class JsonGunModel extends GunModel<
	IGun< ? >,
	IEquippedGun< ? extends IGun< ? > >,
	IEquippedItemRenderer< ? super IEquippedGun< ? extends IGun< ? > > >,
	IGunRenderer<
		? super IGun< ? >,
		? extends IEquippedItemRenderer< ? super IEquippedGun< ? extends IGun< ? > > >
	>
> {
	public static final BuildableLoader< ? >
		LOADER = new BuildableLoader<>( "gun", JsonGunModel.class );
	
	public static final JsonGunModel NONE = new JsonGunModel();
	static { NONE.build( "", MCWBClient.MOD ); }
	
	@Override
	public IGunRenderer<
		? super IGun< ? >,
		? extends IEquippedItemRenderer< ? super IEquippedGun< ? extends IGun< ? > > >
	> newRenderer()
	{
		return this.new GunRenderer()
		{
			@Override
			public IEquippedItemRenderer< ? super IEquippedGun< ? extends IGun< ? > > > onTakeOut(
				EnumHand hand
			) { return this.new EquippedGunRenderer(); }
		};
	};
}
