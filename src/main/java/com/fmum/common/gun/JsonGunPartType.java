package com.fmum.common.gun;

import com.fmum.client.gun.IEquippedGunPartRenderer;
import com.fmum.client.gun.IGunPartRenderer;
import com.fmum.client.gun.JsonGunPartModel;
import com.fmum.client.item.IItemModel;
import com.fmum.common.item.IEquippedItem;
import com.fmum.common.load.BuildableLoader;
import com.fmum.common.meta.IMeta;
import com.fmum.common.module.IModule;

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
		return new GunPart()
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
		final GunPart gunPart = new GunPart( false )
		{
			@Override
			protected IEquippedItem< ? > newEquipped( EntityPlayer player, EnumHand hand ) {
				return new EquippedGunPart( player, hand );
			}
			
			@Override
			protected IEquippedItem< ? > copyEquipped(
				IEquippedItem< ? > target,
				EntityPlayer player,
				EnumHand hand
			) { return new EquippedGunPart( target, player, hand ); }
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
