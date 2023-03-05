package com.mcwb.common.gun;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;

import javax.annotation.Nullable;

import com.google.gson.annotations.SerializedName;
import com.mcwb.client.gun.IGunPartRenderer;
import com.mcwb.common.meta.IMeta;
import com.mcwb.common.module.IModular;
import com.mcwb.common.operation.IOperationController;
import com.mcwb.common.operation.OperationController;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

import com.mcwb.common.ammo.IAmmoType;
import com.mcwb.common.item.IItemTypeHost;
import com.mcwb.common.load.BuildableLoader;

public abstract class MagType< C extends IMag< ? >, R extends IGunPartRenderer< ? super C > >
	extends GunPartType< C, R >
{
	public static final BuildableLoader< IMeta >
		LOADER = new BuildableLoader<>( "mag", MagJson.class );
	
	protected static final OperationController
		PUSH_AMMO_CONTROLLER = new OperationController(
			1F / 10F,
			new float[] { 0.8F },
			new String[ 0 ],
			new float[] { 0.8F },
			"push_ammo"
		),
		POP_AMMO_CONTROLLER = new OperationController(
			1F / 8F,
			new float[] { 0.8F },
			new String[ 0 ],
			new float[] { 0.8F },
			"pop_ammo"
		);
	
	protected Set< String > allowedAmmoCategory = Collections.emptySet();
	
	@SerializedName( value = "ammoCapacity", alternate = "capacity" )
	protected int ammoCapacity = 1;
	
	protected IOperationController pushAmmoController = PUSH_AMMO_CONTROLLER;
	protected IOperationController popAmmoController = POP_AMMO_CONTROLLER;
	
	@Override
	protected IMeta loader() { return LOADER; }
	
	protected class Mag< T extends IGunPart< ? extends T > >
		extends GunPart< T > implements IMag< T >
	{
		protected final ArrayList< IAmmoType > ammo = new ArrayList<>();
		
		protected Mag() { }
		
		protected Mag( boolean unused ) { super( unused ); }
		
		@Override
		public boolean isFull() { return this.ammo.size() >= MagType.this.ammoCapacity; }
		
		@Override
		public int ammoCount() { return this.ammo.size(); }
		
		@Override
		public boolean isAllowed( IAmmoType ammo ) {
			return MagType.this.allowedAmmoCategory.contains( ammo.category() );
		}
		
		@Override
		public void pushAmmo( IAmmoType ammo )
		{
			this.setAmmo( this.nbt.getIntArray( DATA_TAG ), this.ammo.size(), ammo );
			this.ammo.add( ammo );
			this.syncAndUpdate(); // TODO: only sync nbt data
		}
		
		@Override
		public IAmmoType popAmmo()
		{
			final int idx = this.ammo.size() - 1;
			this.setAmmo( this.nbt.getIntArray( DATA_TAG ), idx, null );
			final IAmmoType ammo = this.ammo.remove( idx );
			this.syncAndUpdate();
			return ammo;
		}
		
		@Override
		public IAmmoType getAmmo( int idx ) { return this.ammo.get( idx ); }
		
		@Override
		public IOperationController pushAmmoController() { return MagType.this.pushAmmoController; }
		
		@Override
		public IOperationController popAmmoController() { return MagType.this.popAmmoController; }
		
		@Override
		public void deserializeNBT( NBTTagCompound nbt )
		{
			super.deserializeNBT( nbt );
			
			this.ammo.clear();
			final int[] data = nbt.getIntArray( DATA_TAG );
			for( int i = 0; i < MagType.this.ammoCapacity; ++i )
			{
				final IAmmoType ammo = this.getAmmo( data, i );
				if( ammo != null ) this.ammo.add( ammo );
				else break;
			}
		}
		
		@Override
		protected int dataSize() { return super.dataSize() + MagType.this.ammoCapacity / 2; }
		
		protected void setAmmo( int[] data, int idx, @Nullable IAmmoType ammo )
		{
			final int i = super.dataSize() + idx / 2;
			final int offset = idx % 2 != 0 ? 16 : 0;
			final int id = ammo != null ? Item.getIdFromItem( ammo.item() ) : 0;
			data[ i ] = data[ i ] & 0xFFFF0000 >>> offset | id << offset;
		}
		
		@Nullable
		protected IAmmoType getAmmo( int[] data, int idx )
		{
			final int i = super.dataSize() + idx / 2;
			final int offset = idx % 2 != 0 ? 16 : 0;
			final int id = 0xFFFF & data[ i ] >>> offset;
			if( id == 0 ) return null;
			
			final Item item = Item.getItemById( id );
			return ( IAmmoType ) ( ( IItemTypeHost ) item ).meta();
		}
	}
	
	protected static class MagWrapper<
		M extends IGunPart< ? extends M >,
		T extends IMag< ? extends M >
	> extends GunPartWrapper< M, T > implements IMag< M >
	{
		protected MagWrapper( T primary, ItemStack stack ) { super( primary, stack ); }
		
		@Override
		public boolean isFull() { return this.primary.isFull(); }
		
		@Override
		public int ammoCount() { return this.primary.ammoCount(); }
		
		@Override
		public boolean isAllowed( IAmmoType ammo ) { return this.primary.isAllowed( ammo ); }
		
		@Override
		public void pushAmmo( IAmmoType ammo ) { this.primary.pushAmmo( ammo ); }
		
		@Override
		public IAmmoType popAmmo() { return this.primary.popAmmo(); }
		
		@Override
		public IAmmoType getAmmo( int idx ) { return this.primary.getAmmo( idx ); }
		
		@Override
		public IOperationController pushAmmoController() {
			return this.primary.pushAmmoController();
		}
		
		@Override
		public IOperationController popAmmoController() { return this.primary.popAmmoController(); }
	}
	
	private static class MagJson extends MagType< IMag< ? >, IGunPartRenderer< ? super IMag< ? > > >
	{
		@Override
		public IModular< ? > newRawContexted()
		{
			return this.new Mag< IGunPart< ? > >()
			{
				@Override
				public void syncAndUpdate() { }
			};
		}
		
		@Override
		public IModular< ? > deserializeContexted( NBTTagCompound nbt )
		{
			final Mag< ? > mag = this.new Mag<>( false );
			mag.deserializeNBT( nbt );
			return mag;
		}
		
		@Override
		protected ICapabilityProvider newWrapper( IMag< ? > primary, ItemStack stack ) {
			return new MagWrapper<>( primary, stack );
		}
	}
}
