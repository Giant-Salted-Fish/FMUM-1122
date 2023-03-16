package com.mcwb.client.gun;

import com.mcwb.client.item.IEquippedItemRenderer;
import com.mcwb.common.gun.IEquippedMag;
import com.mcwb.common.gun.IMag;

import net.minecraft.util.EnumHand;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly( Side.CLIENT )
public class JsonMagModel extends MagModel<
	IMag< ? >,
	IEquippedMag< ? extends IMag< ? > >,
	IEquippedItemRenderer< ? super IEquippedMag< ? extends IMag< ? > > >,
	IGunPartRenderer<
		? super IMag< ? >,
		? extends IEquippedItemRenderer< ? super IEquippedMag< ? extends IMag< ? > > >
	>
> {
	@Override
	public IGunPartRenderer<
		? super IMag< ? >,
		? extends IEquippedItemRenderer< ? super IEquippedMag< ? extends IMag< ? > > >
	> newRenderer() {
		return this.new MagRenderer()
		{
			@Override
			public IEquippedItemRenderer< ? super IEquippedMag< ? extends IMag< ? > > > onTakeOut(
				EnumHand hand
			) { return this.new EquippedGunPartRenderer(); }
		};
	}
}
