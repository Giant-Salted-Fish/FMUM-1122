package com.fmum.client.ammo;

import com.fmum.client.item.IEquippedItemRenderer;
import com.fmum.client.item.IItemRenderer;
import com.fmum.client.render.IAnimator;
import com.fmum.common.ammo.IAmmoType;
import com.fmum.common.item.IEquippedItem;
import com.fmum.common.item.IItem;
import com.fmum.common.load.BuildableLoader;
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
	public static final BuildableLoader< ? >
		LOADER = new BuildableLoader<>( "ammo", JsonAmmoModel.class );
	
	public static final JsonAmmoModel NONE = new JsonAmmoModel();
	
	@Override
	public IItemRenderer<
		? super IItem,
		? extends IEquippedItemRenderer< ? super IEquippedItem< ? extends IItem > >
	> newRenderer() { return this; }
	
	@Override
	public IEquippedItemRenderer< ? super IEquippedItem< ? extends IItem > > onTakeOut(
		EnumHand hand
	) { return new EquippedItemRenderer(); }
	
	@Override
	public void render( IItem item, IAnimator animator )
	{
		JsonAmmoModel.this.bindTexture( item.texture() );
		JsonAmmoModel.this.render();
	}
}
