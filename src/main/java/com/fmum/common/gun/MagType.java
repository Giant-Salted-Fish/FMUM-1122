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
import com.fmum.client.player.OperationClient;
import com.fmum.client.player.PlayerPatchClient;
import com.fmum.common.ammo.IAmmoType;
import com.fmum.common.item.IEquippedItem;
import com.fmum.common.item.IItemType;
import com.fmum.common.item.IItemTypeHost;
import com.fmum.common.network.PacketNotifyItem;
import com.fmum.common.player.IOperation;
import com.fmum.common.player.IOperationController;
import com.fmum.common.player.Operation;
import com.fmum.common.player.OperationController;
import com.fmum.common.player.PlayerPatch;
import com.google.gson.annotations.SerializedName;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
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
				case OP_CODE_UNLOAD_AMMO:
					PlayerPatch.get( player ).launch( new OpUnloadAmmo() );
				break;
					
				case OP_CODE_LOAD_AMMO:
					final int ammoInvSlot = buf.readByte();
					PlayerPatch.get( player ).launch( new OpLoadAmmo( ammoInvSlot ) );
				break;
				}
			}
			
			@Override
			@SideOnly( Side.CLIENT )
			protected void setupInputCallbacks( Map< IInput, Runnable > registry )
			{
				final Runnable loadAmmo = () -> PlayerPatchClient
					.instance.launch( new OpLoadAmmoClient() );
				registry.put( Key.PULL_TRIGGER, loadAmmo );
				
				final Runnable unloadAmmo = () -> PlayerPatchClient
					.instance.launch( new OpUnloadAmmoClient() );
				registry.put( Key.AIM_HOLD, unloadAmmo );
				registry.put( Key.AIM_TOGGLE, unloadAmmo );
			}
			
			@Override
			@SideOnly( Side.CLIENT )
			public void onKeyRelease( IInput key )
			{
				final boolean stoppedHolding = (
					key == Key.PULL_TRIGGER
					|| key == Key.AIM_HOLD
					|| key == Key.AIM_TOGGLE
				);
				
				if ( stoppedHolding )
				{
					final IOperation executing = PlayerPatchClient.instance.executing();
					final boolean isLoadingOrUnloadingAmmo = (
						executing instanceof MagType< ?, ?, ?, ?, ?, ? >.Mag.EquippedMag.OpLoadAmmoClient
						|| executing instanceof MagType< ?, ?, ?, ?, ?, ? >.Mag.EquippedMag.OpUnloadAmmoClient
					);
					
					if ( isLoadingOrUnloadingAmmo ) {
						PlayerPatchClient.instance.ternimateExecuting();
					}
				}
			}
			
			@SideOnly( Side.CLIENT )
			protected class OpLoadAmmoClient extends OperationClient< EquippedMag >
			{
				protected OpLoadAmmoClient() {
					super( EquippedMag.this, MagType.this.loadAmmoController );
				}
				
				@Override
				public IOperation launch( EntityPlayer player )
				{
					final boolean alreadyFull = this.equipped.item().isFull();
					if ( alreadyFull ) { return IOperation.NONE; }
					
					final int offset = Key.ASSIST.down ? 1 : 0;
					final int invSlot = this.findValidAmmoSlot( player.inventory, offset );
					final boolean validAmmoNotFound = invSlot == -1;
					if ( validAmmoNotFound ) { return IOperation.NONE; }
					
					this.sendPacketToServer( new PacketNotifyItem( buf -> {
						buf.writeByte( OP_CODE_LOAD_AMMO );
						buf.writeByte( invSlot );
					} ) );
					return this;
				}
				
				@Override
				protected IOperation onComplete( EntityPlayer player )
				{
					super.onComplete( player );
					
					this.clearProgress();
					return this.launch( player );
				}
				
				protected int findValidAmmoSlot( IInventory inv, int offset )
				{
					final IMag< ? > mag = this.equipped.item();
					int invSlot = -1;
					for ( int i = 0, size = inv.getSizeInventory(); offset >= 0 && i < size; ++i )
					{
						final ItemStack stack = inv.getStackInSlot( i );
						final IItemType type = IItemTypeHost.getTypeOrDefault( stack.getItem() );
						final boolean isAmmo = type instanceof IAmmoType;
						final boolean isValidAmmo = isAmmo && mag.isAllowed( ( IAmmoType ) type );
						if ( isValidAmmo )
						{
							invSlot = i;
							--offset;
						}
					}
					return invSlot;
				}
			}
			
			@SideOnly( Side.CLIENT )
			protected class OpUnloadAmmoClient extends OperationClient< EquippedMag >
			{
				protected OpUnloadAmmoClient() {
					super( EquippedMag.this, MagType.this.unloadAmmoController );
				}
				
				@Override
				public IOperation launch( EntityPlayer player )
				{
					final boolean alreadyEmpty = this.equipped.item().isEmpty();
					if ( alreadyEmpty ) { return IOperation.NONE; }
					
					this.sendPacketToServer( new PacketNotifyItem( 
						buf -> buf.writeByte( OP_CODE_UNLOAD_AMMO )
					) );
					return this;
				}
				
				@Override
				protected IOperation onComplete( EntityPlayer player )
				{
					super.onComplete( player );
					
					this.clearProgress();
					return this.launch( player );
				}
			}
		}
		
		protected class OperationOnMag extends Operation
		{
			protected Mag mag;
			
			protected OperationOnMag( IOperationController controller )
			{
				super( controller );
				
				this.mag = Mag.this;
			}
			
			@Override
			@SuppressWarnings( "unchecked" )
			public IOperation onStackUpdate( IEquippedItem< ? > newEquipped, EntityPlayer player )
			{
				this.mag = ( Mag ) newEquipped.item();
				return this;
			}
		}
		
		protected class OpLoadAmmo extends OperationOnMag
		{
			protected final int ammoInvSlot;
			
			protected IOperation next = IOperation.NONE;
			
			protected OpLoadAmmo( int ammoInvSlot )
			{
				super( MagType.this.loadAmmoController );
				
				this.ammoInvSlot = ammoInvSlot;
			}
			
			@Override
			public IOperation launch( EntityPlayer player )
			{
				final boolean alreadyFull = this.mag.isFull();
				if ( alreadyFull ) { return IOperation.NONE; }
				
				final ItemStack stack = player.inventory.getStackInSlot( this.ammoInvSlot );
				final IItemType type = IItemTypeHost.getTypeOrDefault( stack.getItem() );
				final boolean isAmmo = type instanceof IAmmoType;
				final boolean isValidAmmo = isAmmo && this.mag.isAllowed( ( IAmmoType ) type );
				return isValidAmmo ? this : IOperation.NONE;
			}
			
			@Override
			public IOperation onOtherTryLaunch( IOperation op, EntityPlayer player )
			{
				this.next = op;
				return this;
			}
			
			@Override
			protected IOperation onComplete( EntityPlayer player ) {
				return this.next.launch( player );
			}
			
			@Override
			protected void doHandleEffect( EntityPlayer player )
			{
				final ItemStack stack = player.inventory.getStackInSlot( this.ammoInvSlot );
				final IItemType type = IItemTypeHost.getTypeOrDefault( stack.getItem() );
				final boolean isAmmo = type instanceof IAmmoType;
				if ( !isAmmo ) { return; }
				
				final IAmmoType ammo = ( IAmmoType ) type;
				if ( !this.mag.isAllowed( ammo ) ) { return; }
				
				this.mag.pushAmmo( ammo );
//				if ( !this.player.isCreative() )
					stack.shrink( 1 );
			}
		}
		
		protected class OpUnloadAmmo extends OperationOnMag
		{
			protected IOperation next = IOperation.NONE;
			
			protected OpUnloadAmmo() { super( MagType.this.unloadAmmoController ); }
			
			@Override
			public IOperation launch( EntityPlayer player ) {
				return this.mag.isEmpty() ? IOperation.NONE : this;
			}
			
			@Override
			public IOperation onOtherTryLaunch( IOperation op, EntityPlayer player )
			{
				this.next = op;
				return this;
			}
			
			@Override
			protected IOperation onComplete( EntityPlayer player ) {
				return this.next.launch( player );
			}
			
			@Override
			protected void doHandleEffect( EntityPlayer player )
			{
				final IAmmoType ammo = this.mag.popAmmo();
				final ItemStack ammoStack = new ItemStack( ammo.item() );
				player.addItemStackToInventory( ammoStack );
			}
		}
	}
}
