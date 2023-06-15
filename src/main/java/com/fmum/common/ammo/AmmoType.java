package com.fmum.common.ammo;

import com.fmum.client.ammo.IAmmoModel;
import com.fmum.client.item.IEquippedItemRenderer;
import com.fmum.client.item.IItemRenderer;
import com.fmum.client.render.IAnimator;
import com.fmum.common.ModConfig;
import com.fmum.common.item.IEquippedItem;
import com.fmum.common.item.IItem;
import com.fmum.common.item.ItemType;
import com.fmum.common.load.IContentProvider;
import com.fmum.common.meta.IMeta;
import com.google.gson.annotations.SerializedName;
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
import java.util.Optional;
import java.util.Random;

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
	protected static final Random RANDOM = new Random();
	
	protected String category;
	
	protected int maxStackSize = 60;
	
	@SerializedName( value = "notShootable", alternate = "isCase" )
	protected boolean notShootable;
	
	@SerializedName( value = "shellCase", alternate = "ammoCase")
	protected String shellCaseName;
	protected transient IAmmoType shellCase;
	
	@SerializedName( "misfireAmmo" )
	protected String misfireAmmoName;
	protected transient IAmmoType misfireAmmo;
	
	protected float misfirePossibility = 0F;
	
	@Override
	public IMeta build( String name, IContentProvider provider )
	{
		super.build( name, provider );
		IAmmoType.REGISTRY.regis( this );
		
		this.category = Optional.ofNullable( this.category ).orElse( this.name );
		
		final IAmmoType shellCase = IAmmoType.REGISTRY.get( this.shellCaseName );
		this.shellCase = Optional.ofNullable( shellCase ).orElse( this );
		
		final IAmmoType misfireAmmo = IAmmoType.REGISTRY.get( this.misfireAmmoName );
		this.misfireAmmo = Optional.ofNullable( misfireAmmo ).orElse( this );
		return this;
	}
	
	@Override
	public String category() { return this.category; }
	
	@Override
	public IAmmoType onShoot()
	{
		final float possibility = this.misfirePossibility * ModConfig.misfirePossibilityMultiplier;
		final boolean isMisfire = RANDOM.nextFloat() < possibility;
		return isMisfire ? this.misfireAmmo : this;
	}
	
	@Override
	public boolean isShootable() { return !this.notShootable; }
	
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
		@SuppressWarnings( "ConstantValue" )
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
			return this.newEquipped( player, hand );
		}
		
		@Override
		public IEquippedItem< ? > onStackUpdate(
			IEquippedItem< ? > prevEquipped,
			EntityPlayer player,
			EnumHand hand
		) { return newEquipped( player, hand ); }
		
		@Override
		@SideOnly( Side.CLIENT )
		public ResourceLocation texture() { return AmmoType.this.texture; }
		
		final IEquippedItem< ? > newEquipped( EntityPlayer player, EnumHand hand )
		{
			return new IEquippedItem< IItem >()
			{
				IEquippedItemRenderer< ? super IEquippedItem< ? > > renderer;
				{
					if ( player.world.isRemote ) {
						this.renderer = AmmoType.this.model.newRenderer().onTakeOut( hand );
					}
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
