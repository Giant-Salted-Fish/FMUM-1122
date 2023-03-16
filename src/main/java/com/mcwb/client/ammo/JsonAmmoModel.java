package com.mcwb.client.ammo;

import com.mcwb.client.item.IEquippedItemRenderer;
import com.mcwb.client.item.IItemRenderer;
import com.mcwb.client.render.IAnimator;
import com.mcwb.common.ammo.IAmmoType;
import com.mcwb.common.item.IEquippedItem;
import com.mcwb.common.item.IItem;

import net.minecraft.util.EnumHand;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly( Side.CLIENT )
public class JsonAmmoModel extends AmmoModel<
	IAmmoType,
	IItem,
	IEquippedItem< ? extends IItem >,
	IItemRenderer<
		? super IItem,
		? extends IEquippedItemRenderer< ? super IEquippedItem< ? extends IItem > >
	>
> implements IItemRenderer<
	IItem,
	IEquippedItemRenderer< ? super IEquippedItem< ? extends IItem > >
> {
	public static final JsonAmmoModel NONE = new JsonAmmoModel();
	
	@Override
	public IItemRenderer<
		? super IItem,
		? extends IEquippedItemRenderer< ? super IEquippedItem< ? extends IItem > >
	> newRenderer() { return this; }
	
	@Override
	public IEquippedItemRenderer< ? super IEquippedItem< ? extends IItem > > onTakeOut(
		EnumHand hand
	) { return JsonAmmoModel.this.new EquippedItemRenderer(); }
	
	@Override
	public void render( IItem contexted, IAnimator animator )
	{
		JsonAmmoModel.this.bindTexture( contexted.texture() );
		JsonAmmoModel.this.render();
	}
}
