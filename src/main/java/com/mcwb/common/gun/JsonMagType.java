package com.mcwb.common.gun;

import com.mcwb.client.gun.IGunPartRenderer;
import com.mcwb.client.gun.JsonGunPartModel;
import com.mcwb.client.item.IEquippedItemRenderer;
import com.mcwb.client.item.IItemModel;
import com.mcwb.common.item.IEquippedItem;
import com.mcwb.common.module.IModule;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumHand;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

public class JsonMagType extends MagType<
	IGunPart< ? >,
	IMag< ? >,
	IEquippedMag< ? extends IMag< ? > >,
	IEquippedItemRenderer< ? super IEquippedMag< ? extends IMag< ? > > >,
	IGunPartRenderer<
		? super IMag< ? >,
		? extends IEquippedItemRenderer< ? super IEquippedMag< ? extends IMag< ? > > >
	>,
	IItemModel< ? extends IGunPartRenderer<
		? super IMag< ? >,
		? extends IEquippedItemRenderer< ? super IEquippedMag< ? extends IMag< ? > > >
	> >
> {
	@Override
	public IModule< ? > newRawContexted()
	{
		return this.new Mag()
		{
			// Override this so that we do not need to create a wrapper for it
			@Override
			public void syncAndUpdate() { }
			
			@Override
			public IEquippedItem< ? > onTakeOut( EntityPlayer player, EnumHand hand ) {
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
		final Mag mag = this.new Mag( false )
		{
			@Override
			public IEquippedItem< ? > onTakeOut( EntityPlayer player, EnumHand hand ) {
				return this.new EquippedMag( () -> this.renderer.onTakeOut( hand ), player, hand );
			}
			
			@Override
			@SuppressWarnings( "unchecked" )
			public IEquippedItem< ? > onStackUpdate(
				IEquippedItem< ? > prevEquipped,
				EntityPlayer player,
				EnumHand hand
			) {
				final EquippedMag prev = ( EquippedMag ) prevEquipped;
				return this.new EquippedMag( () -> prev.renderer, player, hand );
			}
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
		? extends IEquippedItemRenderer< ? super IEquippedMag< ? extends IMag< ? > > > >
	> fallbackModel() { return JsonGunPartModel.NONE; }
}
