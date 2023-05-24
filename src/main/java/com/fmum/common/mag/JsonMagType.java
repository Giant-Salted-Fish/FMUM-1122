package com.fmum.common.mag;

import com.fmum.client.gun.IEquippedGunPartRenderer;
import com.fmum.client.gun.IGunPartRenderer;
import com.fmum.client.gun.JsonGunPartModel;
import com.fmum.client.item.IItemModel;
import com.fmum.common.gun.IGunPart;
import com.fmum.common.item.IEquippedItem;
import com.fmum.common.load.BuildableLoader;
import com.fmum.common.meta.IMeta;
import com.fmum.common.module.IModule;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumHand;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

public class JsonMagType extends MagType<
	IGunPart< ? >,
	IMag< ? >,
	IEquippedMag< ? extends IMag< ? > >,
	IEquippedGunPartRenderer< ? super IEquippedItem< ? extends IMag< ? > > >,
	IGunPartRenderer<
		? super IMag< ? >,
		? extends IEquippedGunPartRenderer< ? super IEquippedItem< ? extends IMag< ? > > >
	>,
	IItemModel< ? extends IGunPartRenderer<
		? super IMag< ? >,
		? extends IEquippedGunPartRenderer< ? super IEquippedItem< ? extends IMag< ? > > >
	> >
> {
	public static final BuildableLoader< IMeta >
		LOADER = new BuildableLoader<>( "mag", JsonMagType.class );
	
	@Override
	public IModule< ? > newRawContexted()
	{
		return new Mag()
		{
			// Override this so that we do not need to create a wrapper for it.
			@Override
			public void syncAndUpdate() { }
			
			// This should never be equipped hence return null.
			@Override
			protected IEquippedItem< ? > newEquipped( EntityPlayer player, EnumHand hand ) {
				return null;
			}
			
			@Override
			protected IEquippedItem< ? > copyEquipped(
				IEquippedItem< ? > target,
				EntityPlayer player,
				EnumHand hand
			) { return null; }
		};
	}
	
	@Override
	public IModule< ? > deserializeContexted( NBTTagCompound nbt )
	{
		final Mag mag = new Mag( false )
		{
			@Override
			protected IEquippedItem< ? > newEquipped( EntityPlayer player, EnumHand hand ) {
				return new EquippedMag( player, hand );
			}
			
			@Override
			protected IEquippedItem< ? > copyEquipped(
				IEquippedItem< ? > target,
				EntityPlayer player,
				EnumHand hand
			) { return new EquippedMag( target, player, hand ); }
		};
		mag.deserializeNBT( nbt );
		return mag;
	}
	
	@Override
	protected ICapabilityProvider newWrapper( IMag< ? > primary, ItemStack stack ) {
		return new MagWrapper<>( primary, stack );
	}
	
	@Override
	protected IItemModel< ? extends IGunPartRenderer<
		? super IMag< ? >,
		? extends IEquippedGunPartRenderer< ? super IEquippedItem< ? extends IMag< ? > > > >
	> fallbackModel() { return JsonGunPartModel.NONE; }
	
	@Override
	protected IMeta descriptor() { return LOADER; }
}
