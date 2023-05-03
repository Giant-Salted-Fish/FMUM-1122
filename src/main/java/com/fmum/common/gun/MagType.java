package com.fmum.common.gun;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import com.fmum.client.gun.IEquippedGunPartRenderer;
import com.fmum.client.gun.IGunPartRenderer;
import com.fmum.client.input.IInput;
import com.fmum.client.input.Key;
import com.fmum.client.item.IItemModel;
import com.fmum.client.player.OpLoadAmmoClient;
import com.fmum.client.player.OpUnloadAmmoClient;
import com.fmum.client.player.PlayerPatchClient;
import com.fmum.common.ammo.IAmmoType;
import com.fmum.common.item.IItemTypeHost;
import com.fmum.common.network.PacketNotifyItem;
import com.fmum.common.operation.IOperation;
import com.fmum.common.operation.IOperationController;
import com.fmum.common.operation.OperationController;
import com.fmum.common.player.OpLoadAmmo;
import com.fmum.common.player.OpUnloadAmmo;
import com.fmum.common.player.PlayerPatch;
import com.google.gson.annotations.SerializedName;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumHand;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public abstract class MagType<
	I extends IGunPart< ? extends I >,
	C extends IMag< ? >,
	E extends IEquippedMag< ? extends C >,
	ER extends IEquippedGunPartRenderer< ? super E >,
	R extends IGunPartRenderer< ? super C, ? extends ER >,
	M extends IItemModel< ? extends R >
> extends GunPartType< I, C, E, ER, R, M >
{
	protected static final OperationController
	LOAD_AMMO_CONTROLLER = new OperationController(
		1F / 10F,
		new float[] { 0.8F },
		new String[ 0 ],
		new float[] { 0.8F },
		"load_ammo"
	),
	UNLOAD_AMMO_CONTROLLER = new OperationController(
		1F / 8F,
		new float[] { 0.8F },
		new String[ 0 ],
		new float[] { 0.8F },
		"unload_ammo"
	);
	
	protected Set< String > allowedAmmoCategory = Collections.emptySet();
	
	@SerializedName( value = "ammoCapacity", alternate = "capacity" )
	protected int ammoCapacity = 1;
	
	protected IOperationController loadAmmoController = LOAD_AMMO_CONTROLLER;
	protected IOperationController unloadAmmoController = UNLOAD_AMMO_CONTROLLER;
	
	protected abstract class Mag extends GunPart implements IMag< I >
	{
		protected final ArrayList< IAmmoType > ammo = new ArrayList<>();
		
		@SideOnly( Side.CLIENT )
		protected transient boolean isLoadingMag;
		
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
		@SideOnly( Side.CLIENT )
		public boolean isLoadingMag() { return this.isLoadingMag; }
		
		@Override
		@SideOnly( Side.CLIENT )
		public void setAsLoadingMag() { this.isLoadingMag = true; }
		
		@Override
		public void deserializeNBT( NBTTagCompound nbt )
		{
			super.deserializeNBT( nbt );
			
			this.ammo.clear();
			final int[] data = nbt.getIntArray( DATA_TAG );
			for ( int i = 0; i < MagType.this.ammoCapacity; ++i )
			{
				final IAmmoType ammo = this.getAmmo( data, i );
				if ( ammo == null ) { break; }
				this.ammo.add( ammo );
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
			final boolean isOddIdx = idx % 2 != 0;
			final int offset = isOddIdx ? 16 : 0;
			final int id = 0xFFFF & data[ i ] >>> offset;
			if ( id == 0 ) { return null; }
			
			final Item item = Item.getItemById( id );
			return ( IAmmoType ) ( ( IItemTypeHost ) item ).meta();
		}
		
		protected class EquippedMag extends EquippedGunPart implements IEquippedMag< C >
		{
			protected static final byte
				OP_CODE_LOAD_AMMO = 0,
				OP_CODE_UNLOAD_AMMO = 1;
			
			protected EquippedMag(
				Supplier< ER > equippedRenderer,
				Supplier< Function< E, E > > renderDelegate,
				EntityPlayer player,
				EnumHand hand
			) { super( equippedRenderer, renderDelegate, player, hand ); }
			
			@Override
			public void handlePacket( ByteBuf buf, EntityPlayer player )
			{
				switch ( buf.readByte() )
				{
				case OP_CODE_LOAD_AMMO: {
					final int invSlot = buf.readByte();
					final IOperationController controller = MagType.this.loadAmmoController;
					final IOperation op = new OpLoadAmmo( Mag.this, invSlot, controller );
					PlayerPatch.get( player ).launch( op );
				break; }
					
				case OP_CODE_UNLOAD_AMMO:
					final IOperationController controller = MagType.this.unloadAmmoController;
					final IOperation op = new OpUnloadAmmo( Mag.this, controller );
					PlayerPatch.get( player ).launch( op );
				}
			}
			
			@Override
			@SideOnly( Side.CLIENT )
			protected void setupInputCallbacks( Map< IInput, Runnable > registry )
			{
				final Runnable loadAmmo = () -> PlayerPatchClient.instance.launch(
					new OpLoadAmmoClient( this, MagType.this.loadAmmoController )
					{
						@Override
						protected void launchCallback()
						{
							this.sendPacketToServer( new PacketNotifyItem( buf -> {
								buf.writeByte( OP_CODE_LOAD_AMMO );
								buf.writeByte( this.invSlot );
							} ) );
						}
					}
				);
				registry.put( Key.PULL_TRIGGER, loadAmmo );
				
				final Runnable unloadAmmo = () -> PlayerPatchClient.instance.launch(
					new OpUnloadAmmoClient( this, MagType.this.unloadAmmoController )
					{
						@Override
						protected void launchCallback()
						{
							this.sendPacketToServer( new PacketNotifyItem( 
								buf -> buf.writeByte( OP_CODE_UNLOAD_AMMO )
							) );
						}
					}
				);
				registry.put( Key.AIM_HOLD, unloadAmmo );
				registry.put( Key.AIM_TOGGLE, unloadAmmo );
			}
			
			@Override
			@SideOnly( Side.CLIENT )
			public void onKeyRelease( IInput key )
			{
				final boolean stopped = (
					key == Key.PULL_TRIGGER
					|| key == Key.AIM_HOLD
					|| key == Key.AIM_TOGGLE
				);
				
				if ( stopped )
				{
					final IOperation executing = PlayerPatchClient.instance.executing();
					final boolean loadingOrUnloading = (
						executing instanceof OpLoadAmmoClient
						|| executing instanceof OpUnloadAmmoClient
					);
					
					if ( loadingOrUnloading ) {
						PlayerPatchClient.instance.ternimateExecuting();
					}
				}
			}
		}
	}
}
