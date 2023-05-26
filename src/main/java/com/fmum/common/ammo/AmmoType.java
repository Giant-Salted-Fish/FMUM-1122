package com.fmum.common.ammo;

import com.fmum.client.ammo.IAmmoModel;
import com.fmum.client.item.IEquippedItemRenderer;
import com.fmum.client.item.IItemRenderer;
import com.fmum.client.render.IAnimator;
import com.fmum.common.FMUM;
import com.fmum.common.item.IEquippedItem;
import com.fmum.common.item.IItem;
import com.fmum.common.item.ItemType;
import com.fmum.common.load.IContentProvider;
import com.fmum.common.meta.IMeta;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class AmmoType<
	C extends IItem,
	M extends IAmmoModel<
		? super IAmmoType,
		? extends IItemRenderer<
			? super IItem,
			? extends IEquippedItemRenderer< ? super IEquippedItem< ? > >
		>
	>
> extends ItemType< C, M > implements IAmmoType
{
	protected String category;
	
	protected boolean isCase = false;
	
	protected int maxStackSize = 60;
	
	@Override
	public IMeta build( String name, IContentProvider provider )
	{
		super.build( name, provider );
		
		IAmmoType.REGISTRY.regis( this );
		
		this.category = this.category == null ? this.name : this.category;
		return this;
	}
	
	@Override
	public String category() { return this.category; }
	
	@Override
	public boolean isCase() { return this.isCase; }
	
	@Override
	@SideOnly( Side.CLIENT )
	public void render() { this.model.render( this ); }
	
	@Override
	@SideOnly( Side.CLIENT )
	public ResourceLocation texture() { return this.texture; }
	
	@Override
	protected Item createItem()
	{
		return new VanillaItem( this.maxStackSize, 0 )
		{
			@Override
			public ICapabilityProvider initCapabilities(
				ItemStack stack,
				@Nullable NBTTagCompound capTag
			) { return new Ammo(); }
		};
	}
	
	protected class Ammo implements IItem, ICapabilityProvider
	{
		@Override
		public boolean hasCapability(
			@Nonnull Capability< ? > capability,
			@Nullable EnumFacing facing
		) { return capability == CAPABILITY; }
		
		@Override
		public < T > T getCapability(
			@Nonnull Capability< T > capability,
			@Nullable EnumFacing facing
		) { return CAPABILITY.cast( this ); }
		
		@Override
		public int stackId() { return this.hashCode(); }
		
		@Override
		public IEquippedItem< ? > onTakeOut( EntityPlayer player, EnumHand hand ) {
			return newEquipped( hand );
		}
		
		@Override
		public IEquippedItem< ? > onStackUpdate(
			IEquippedItem< ? > prevEquipped,
			EntityPlayer player,
			EnumHand hand
		) { return newEquipped( hand ); }
		
		@Override
		@SideOnly( Side.CLIENT )
		public ResourceLocation texture() { return AmmoType.this.texture; }
		
		final IEquippedItem< ? > newEquipped( EnumHand hand )
		{
			return new IEquippedItem< IItem >()
			{
				IEquippedItemRenderer< ? super IEquippedItem< ? > > renderer;
				{
					// TODO: this will create instance on local server
					FMUM.MOD.clientOnly(
						() -> this.renderer = AmmoType.this.model.newRenderer().onTakeOut( hand )
					);
				}
				
				@Override
				public IItem item() { return Ammo.this; }
				
				@Override
				@SideOnly( Side.CLIENT )
				public boolean renderInHandSP( EnumHand hand ) {
					return this.renderer.renderInHandSP( this, hand );
				}
				
				@Override
				@SideOnly( Side.CLIENT )
				public boolean onRenderSpecificHandSP( EnumHand hand ) {
					return this.renderer.onRenderSpecificHandSP( this, hand );
				}
				
				@Override
				@SideOnly( Side.CLIENT )
				public IAnimator animator() { return this.renderer.animator(); }
			};
		}
	}
}
