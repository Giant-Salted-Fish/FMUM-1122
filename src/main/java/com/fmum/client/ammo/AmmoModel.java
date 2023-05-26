package com.fmum.client.ammo;

import com.fmum.client.IAutowireBindTexture;
import com.fmum.client.item.IEquippedItemRenderer;
import com.fmum.client.item.IItemRenderer;
import com.fmum.client.item.ItemModel;
import com.fmum.common.ammo.IAmmoType;
import com.fmum.common.item.IEquippedItem;
import com.fmum.common.item.IItem;
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
