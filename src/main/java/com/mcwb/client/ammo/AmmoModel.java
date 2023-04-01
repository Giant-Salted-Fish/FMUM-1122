package com.mcwb.client.ammo;

import com.mcwb.client.IAutowireBindTexture;
import com.mcwb.client.item.IEquippedItemRenderer;
import com.mcwb.client.item.IItemRenderer;
import com.mcwb.client.item.ItemModel;
import com.mcwb.common.ammo.IAmmoType;
import com.mcwb.common.item.IEquippedItem;
import com.mcwb.common.item.IItem;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly( Side.CLIENT )
public abstract class AmmoModel<
	T extends IAmmoType,
	C extends IItem,
	E extends IEquippedItem< ? extends C >,
	R extends IItemRenderer< ? super C, ? extends IEquippedItemRenderer< ? super E > >
> extends ItemModel< C, E, R > implements IAmmoModel< T, R >, IAutowireBindTexture
{
	@Override
	public void render( T type )
	{
		this.bindTexture( type.texture() );
		this.render();
	}
}
