package com.mcwb.common.gun;

import com.mcwb.client.gun.IGunPartRenderer;
import com.mcwb.client.gun.JsonGunModel;
import com.mcwb.client.item.IEquippedItemRenderer;
import com.mcwb.client.item.IItemModel;
import com.mcwb.common.item.IEquippedItem;
import com.mcwb.common.module.IModule;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumHand;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

public class JsonGunType extends GunType<
	IGunPart< ? >,
	IGun< ? >,
	IEquippedGun< ? extends IGun< ? > >,
	IEquippedItemRenderer< ? super IEquippedGun< ? extends IGun< ? > > >,
	IGunPartRenderer<
		? super IGun< ? >,
		? extends IEquippedItemRenderer< ? super IEquippedGun< ? extends IGun< ? > > >
	>,
	IItemModel< ? extends IGunPartRenderer<
		? super IGun< ? >,
		? extends IEquippedItemRenderer< ? super IEquippedGun< ? extends IGun< ? > > >
	> >
> {
	@Override
	public IModule< ? > newRawContexted()
	{
		return this.new Gun()
		{
			// Override this so that we do not need to create a wrapper for it
			@Override
			public void syncAndUpdate() { }
			
			@Override
			public IEquippedItem< ? > onTakeOut( EntityPlayer player, EnumHand hand ) { // TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public IEquippedItem< ? > onStackUpdate(
				IEquippedItem< ? > prevEquipped,
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
			public IEquippedItem< ? > onTakeOut( EntityPlayer player, EnumHand hand ) {
				return this.new EquippedGun( () -> this.renderer.onTakeOut( hand ), player, hand );
			}
			
			@Override
			@SuppressWarnings( "unchecked" )
			public IEquippedItem< ? > onStackUpdate(
				IEquippedItem< ? > prevEquipped,
				EntityPlayer player,
				EnumHand hand
			) {
				final EquippedGun prev = ( EquippedGun ) prevEquipped;
				final EquippedGun cur = this.new EquippedGun( () -> prev.renderer, player, hand );
				// TODO: copy useful data
				return cur;
			}
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
		? extends IEquippedItemRenderer< ? super IEquippedGun< ? extends IGun< ? > > >
	> > fallbackModel() { return JsonGunModel.NONE; }
}
