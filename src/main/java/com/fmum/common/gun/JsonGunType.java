package com.fmum.common.gun;

import java.util.function.Function;
import java.util.function.Supplier;

import com.fmum.client.gun.IEquippedGunPartRenderer;
import com.fmum.client.gun.IGunPartRenderer;
import com.fmum.client.gun.JsonGunModel;
import com.fmum.client.item.IItemModel;
import com.fmum.common.load.BuildableLoader;
import com.fmum.common.meta.IMeta;
import com.fmum.common.module.IModule;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumHand;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

public class JsonGunType extends GunType<
	IGunPart< ? >,
	IGun< ? >,
	IEquippedGun< ? extends IGun< ? > >,
	IEquippedGunPartRenderer< ? super IEquippedGun< ? extends IGun< ? > > >,
	IGunPartRenderer<
		? super IGun< ? >,
		? extends IEquippedGunPartRenderer< ? super IEquippedGun< ? extends IGun< ? > > >
	>,
	IItemModel< ? extends IGunPartRenderer<
		? super IGun< ? >,
		? extends IEquippedGunPartRenderer< ? super IEquippedGun< ? extends IGun< ? > > >
	> >
> {
	public static final BuildableLoader< IMeta >
		LOADER = new BuildableLoader<>( "gun", JsonGunType.class );
	
	@Override
	public IModule< ? > newRawContexted()
	{
		return this.new Gun()
		{
			// Override this so that we do not need to create a wrapper for it.
			@Override
			public void syncAndUpdate() { }
			
			// This should never be equipped hence return null.
			@Override
			protected IEquippedGun< ? extends IGun< ? > > newEquipped(
				Supplier< IEquippedGunPartRenderer< ? super IEquippedGun< ? extends IGun< ? > > > >
					equippedRenderer,
				Supplier< Function<
					IEquippedGun< ? extends IGun< ? > >,
					IEquippedGun< ? extends IGun< ? > >
				> > renderDelegate,
				EntityPlayer player,
				EnumHand hand
			) { return null; }
		};
	}
	
	@Override
	public IModule< ? > deserializeContexted( NBTTagCompound nbt )
	{
		final Gun gun = this.new Gun( false )
		{
			@Override
			protected IEquippedGun< ? extends IGun< ? > > newEquipped(
				Supplier< IEquippedGunPartRenderer< ? super IEquippedGun< ? extends IGun< ? > > > >
					equippedRenderer,
				Supplier< Function<
					IEquippedGun< ? extends IGun< ? > >,
					IEquippedGun< ? extends IGun< ? > >
				> > renderDelegate,
				EntityPlayer player,
				EnumHand hand
			) { return this.new EquippedGun( equippedRenderer, renderDelegate, player, hand ); }
		};
		gun.deserializeNBT( nbt );
		return gun;
	}
	
	@Override
	protected ICapabilityProvider newWrapper( IGun< ? > primary, ItemStack stack ) {
		return new GunWrapper<>( primary, stack );
	}
	
	@Override
	protected IItemModel< ? extends IGunPartRenderer<
		? super IGun< ? >,
		? extends IEquippedGunPartRenderer< ? super IEquippedGun< ? extends IGun< ? > > >
	> > fallbackModel() { return JsonGunModel.NONE; }
	
	@Override
	protected IMeta descriptor() { return LOADER; }
}
