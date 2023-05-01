package com.fmum.common.ammo;

import com.fmum.client.ammo.IAmmoModel;
import com.fmum.client.ammo.JsonAmmoModel;
import com.fmum.client.item.IEquippedItemRenderer;
import com.fmum.client.item.IItemRenderer;
import com.fmum.common.item.IEquippedItem;
import com.fmum.common.item.IItem;
import com.fmum.common.load.BuildableLoader;
import com.fmum.common.meta.IMeta;

public class JsonAmmoType extends AmmoType<
	IItem,
	IAmmoModel<
		? super IAmmoType,
		? extends IItemRenderer<
			? super IItem,
			? extends IEquippedItemRenderer< ? super IEquippedItem< ? > >
		>
	>
> {
	public static final BuildableLoader< IMeta >
		LOADER = new BuildableLoader<>( "ammo", JsonAmmoType.class );
	
	@Override
	protected IAmmoModel<
		? super IAmmoType,
		? extends IItemRenderer<
			? super IItem,
			? extends IEquippedItemRenderer< ? super IEquippedItem< ? > >
		>
	> fallbackModel() { return JsonAmmoModel.NONE; }
	
	@Override
	protected IMeta descriptor() { return LOADER; }
}
