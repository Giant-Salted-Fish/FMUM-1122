package com.mcwb.client.gun;

import com.mcwb.client.MCWBClient;
import com.mcwb.client.item.IEquippedItemRenderer;
import com.mcwb.common.gun.IGunPart;
import com.mcwb.common.item.IEquippedItem;

import net.minecraft.util.EnumHand;

public class JsonGunPartModel extends GunPartModel<
	IGunPart< ? >,
	IEquippedItem< ? extends IGunPart< ? > >,
	IEquippedItemRenderer< ? super IEquippedItem< ? extends IGunPart< ? > > >,
	IGunPartRenderer<
		? super IGunPart< ? >,
		? extends IEquippedItemRenderer< ? super IEquippedItem< ? extends IGunPart< ? > > >
	>
> {
	public static final JsonGunPartModel NONE = new JsonGunPartModel();
	static { NONE.build( "", MCWBClient.MOD ); }
	
	@Override
	public IGunPartRenderer<
		? super IGunPart< ? >,
		? extends IEquippedItemRenderer< ? super IEquippedItem< ? extends IGunPart< ? > > >
	> newRenderer()
	{
		return this.new GunPartRenderer()
		{
			@Override
			public IEquippedItemRenderer<
				? super IEquippedItem< ? extends IGunPart< ? > >
			> onTakeOut( EnumHand hand )
			{ return this.new EquippedGunPartRenderer(); }
		};
	}
}
