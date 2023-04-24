package com.mcwb.common.gun;

import java.util.function.Function;
import java.util.function.Supplier;

import com.mcwb.client.gun.IEquippedGunPartRenderer;
import com.mcwb.client.gun.IGunPartRenderer;
import com.mcwb.client.gun.JsonGunPartModel;
import com.mcwb.client.item.IItemModel;
import com.mcwb.common.item.IEquippedItem;
import com.mcwb.common.load.BuildableLoader;
import com.mcwb.common.meta.IMeta;
import com.mcwb.common.module.IModule;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumHand;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

// Emmm... Looks like the vast generic sea :-)
public class JsonGunPartType extends GunPartType<
	IGunPart< ? >,
	IGunPart< ? >,
	IEquippedItem< ? extends IGunPart< ? > >,
	IEquippedGunPartRenderer< ? super IEquippedItem< ? extends IGunPart< ? > > >,
	IGunPartRenderer<
		? super IGunPart< ? >,
		? extends IEquippedGunPartRenderer< ? super IEquippedItem< ? extends IGunPart< ? > > >
	>,
	IItemModel< ? extends IGunPartRenderer<
		? super IGunPart< ? >,
		? extends IEquippedGunPartRenderer< ? super IEquippedItem< ? extends IGunPart< ? > > >
	> >
> {
	public static final BuildableLoader< IMeta >
		LOADER = new BuildableLoader<>( "gun_part", JsonGunPartType.class );
	
	@Override
	public IModule< ? > newRawContexted()
	{
		return this.new GunPart()
		{
			// Override this so that we do not need to create a wrapper for it.
			@Override
			public void syncAndUpdate() { }
			
			// This should never be equipped hence return null.
			@Override
			protected IEquippedItem< ? extends IGunPart< ? > > newEquipped(
				Supplier<
					IEquippedGunPartRenderer< ? super IEquippedItem< ? extends IGunPart< ? > > >
				> equippedRenderer,
				Supplier< Function<
					IEquippedItem< ? extends IGunPart< ? > >,
					IEquippedItem< ? extends IGunPart< ? > >
				> > renderDelegate,
				EntityPlayer player,
				EnumHand hand
			) { return null; }
		};
	}
	
	@Override
	public IModule< ? > deserializeContexted( NBTTagCompound nbt )
	{
		final GunPart gunPart = this.new GunPart( false )
		{
			@Override
			protected IEquippedItem< ? extends IGunPart< ? > > newEquipped(
				Supplier<
					IEquippedGunPartRenderer< ? super IEquippedItem< ? extends IGunPart< ? > > >
				> equippedRenderer,
				Supplier< Function<
					IEquippedItem< ? extends IGunPart< ? > >,
					IEquippedItem< ? extends IGunPart< ? > >
				> > renderDelegate,
				EntityPlayer player,
				EnumHand hand
			) { return this.new EquippedGunPart( equippedRenderer, renderDelegate, player, hand ); }
		};
		gunPart.deserializeNBT( nbt );
		return gunPart;
	}
	
	@Override
	protected ICapabilityProvider newWrapper( IGunPart< ? > primary, ItemStack stack ) {
		return new GunPartWrapper<>( primary, stack );
	}
	
	@Override
	protected IItemModel< ? extends IGunPartRenderer<
		? super IGunPart< ? >,
		? extends IEquippedGunPartRenderer< ? super IEquippedItem< ? extends IGunPart< ? > > >
	> > fallbackModel() { return JsonGunPartModel.NONE; }
	
	@Override
	protected IMeta descriptor() { return LOADER; }
}
