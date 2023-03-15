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

// Emmm... Looks like the vast generic sea
public class JsonGunPartType extends GunPartType<
	IGunPart< ? >,
	IGunPart< ? >,
	IEquippedItem< ? extends IGunPart< ? > >,
	IEquippedItemRenderer< ? super IEquippedItem< ? extends IGunPart< ? > > >,
	IGunPartRenderer<
		? super IGunPart< ? >,
		? extends IEquippedItemRenderer< ? super IEquippedItem< ? extends IGunPart< ? > > >
	>,
	IItemModel< ? extends IGunPartRenderer<
		? super IGunPart< ? >,
		? extends IEquippedItemRenderer< ? super IEquippedItem< ? extends IGunPart< ? > > >
	> >
> {
	@Override
	public IModule< ? > newRawContexted()
	{
		return this.new GunPart()
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
		final GunPart gunPart = this.new GunPart( false )
		{
			@Override
			public IEquippedItem< ? > onTakeOut( EntityPlayer player, EnumHand hand )
			{
				return this.new EquippedGunPart(
					() -> this.renderer.onTakeOut( hand ),
					player, hand
				);
			}
			
			@Override
			@SuppressWarnings( "unchecked" )
			public IEquippedItem< ? > onStackUpdate(
				IEquippedItem< ? > prevEquipped,
				EntityPlayer player,
				EnumHand hand
			) {
				final EquippedGunPart prev = ( EquippedGunPart ) prevEquipped;
				return this.new EquippedGunPart( () -> prev.renderer, player, hand );
			}
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
		? extends IEquippedItemRenderer< ? super IEquippedItem< ? extends IGunPart< ? > > >
	> > fallbackModel() { return JsonGunPartModel.NONE; }
}
