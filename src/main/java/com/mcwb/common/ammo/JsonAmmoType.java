package com.mcwb.common.ammo;

import com.mcwb.client.ammo.IAmmoModel;
import com.mcwb.client.ammo.JsonAmmoModel;
import com.mcwb.client.item.IEquippedItemRenderer;
import com.mcwb.client.item.IItemRenderer;
import com.mcwb.common.item.IEquippedItem;
import com.mcwb.common.item.IItem;

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
	@Override
	protected IAmmoModel<
		? super IAmmoType,
		? extends IItemRenderer<
			? super IItem,
			? extends IEquippedItemRenderer< ? super IEquippedItem< ? > >
		>
	> fallbackModel() { return JsonAmmoModel.NONE; }
}
