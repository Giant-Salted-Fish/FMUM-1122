package com.mcwb.client.gun;

import com.mcwb.client.item.IEquippedItemRenderer;
import com.mcwb.common.gun.IGunPart;
import com.mcwb.common.item.IEquippedItem;

import net.minecraft.util.EnumHand;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly( Side.CLIENT )
public class JsonGripModel extends GripModel<
	IGunPart< ? >,
	IEquippedItem< ? extends IGunPart< ? > >,
	IEquippedItemRenderer< ? super IEquippedItem< ? extends IGunPart< ? > > >,
	IGunPartRenderer<
		? super IGunPart< ? >,
		? extends IEquippedItemRenderer< ? super IEquippedItem< ? extends IGunPart< ? > > >
	>
> {
	@Override
	public IGunPartRenderer< ? super IGunPart< ? >, ? extends IEquippedItemRenderer< ? super IEquippedItem< ? extends IGunPart< ? > > > > newRenderer()
	{
		return this.new GripRenderer()
		{
			@Override
			public IEquippedItemRenderer<
				? super IEquippedItem< ? extends IGunPart< ? > >
			> onTakeOut( EnumHand hand ) { return this.new EquippedGunPartRenderer(); }
		};
	}
}
