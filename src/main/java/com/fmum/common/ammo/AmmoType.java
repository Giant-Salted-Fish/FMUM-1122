package com.fmum.common.ammo;

import com.fmum.common.item.IEquippedItem;
import com.fmum.common.item.IFMUMVanillaItem;
import com.fmum.common.item.IItem;
import com.fmum.common.item.IItemType;
import com.fmum.common.item.ItemType;
import com.fmum.common.load.IContentBuildContext;
import com.fmum.util.Category;
import com.google.gson.annotations.SerializedName;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;

public class AmmoType extends ItemType implements IAmmoType
{
	protected Category category;
	
	protected int max_stack_size = 60;
	
	protected boolean canShoot = true;
	
	@SerializedName( value = "shell_case", alternate = "ammo_case" )
	protected String shell_case_name = "";
	protected transient IAmmoType shell_case;
	
	@SerializedName( value = "misfire_ammo" )
	protected String misfire_ammo_name = "";
	protected transient IAmmoType misfire_ammo;
	
	protected float misfire_chance = 0.0F;
	
	@Override
	public void buildServerSide( IContentBuildContext ctx )
	{
		super.buildServerSide( ctx );
		
		IAmmoType.REGISTRY.regis( this );
		
		this.category = Optional.ofNullable( this.category )
			.orElseGet( () -> new Category( this.name ) );
		
		this.shell_case = IAmmoType.REGISTRY
			.lookup( this.shell_case_name ).orElse( this );
		this.shell_case_name = null;
		
		this.misfire_ammo = IAmmoType.REGISTRY
			.lookup( this.misfire_ammo_name ).orElse( this );
		this.misfire_ammo_name = null;
	}
	
	@Override
	public Category category() {
		return this.category;
	}
	
	@Override
	public boolean canShoot() {
		return this.canShoot;
	}
	
	@Override
	protected Item _createItem( IContentBuildContext ctx ) {
		return new _AmmoItem();
	}
	
	@Override
	protected String _typeHint() {
		return "AMMO";
	}
	
	
	protected class _AmmoItem extends Item implements IFMUMVanillaItem
	{
		protected _AmmoItem() {
			this.setMaxStackSize( AmmoType.this.max_stack_size );
		}
		
		@Override
		public final IItemType type() {
			return AmmoType.this;
		}
		
		@Override
		public ICapabilityProvider initCapabilities(
			@Nonnull ItemStack stack,
			@Nullable NBTTagCompound nbt
		) { return new _Ammo(); }
	}
	
	
	protected class _Ammo implements IItem, ICapabilityProvider
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
		public int stackId() {
			return this.hashCode();
		}
		
		@Override
		public IEquippedItem< ? > onTakeOut( EntityPlayer player, EnumHand hand )
		{
			return new IEquippedItem< IItem >()
			{
				@Override
				public IItem item() {
					return _Ammo.this;
				}
				
				@Override
				public boolean onRenderHand( EnumHand hand )
				{
					return false;
				}
				
				@Override
				public boolean onRenderSpecificHand( EnumHand hand )
				{
					return false;
				}
			};
		}
	}
}
