package com.fmum.common.gun;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import com.fmum.client.FMUMClient;
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
		protected static final String
			LIST_AMMO_TAG = "l",
			COUNT_AMMO_TAG = "c";
		
		protected IAmmoNBTHandler ammoNBTHandler;
		
		@SideOnly( Side.CLIENT )
		protected transient boolean isLoadingMag;
		
		protected Mag() { this.ammoNBTHandler = new CountAmmoNBTHandler(); }
		
		protected Mag( boolean unused ) { super( unused ); }
		
		@Override
		public boolean isFull() {
			return this.ammoNBTHandler.ammoCount() >= MagType.this.ammoCapacity;
		}
		
		@Override
		public int ammoCount() { return this.ammoNBTHandler.ammoCount(); }
		
		@Override
		public boolean isAllowed( IAmmoType ammo ) {
			return MagType.this.allowedAmmoCategory.contains( ammo.category() );
		}
		
		@Override
		public void forEachAmmo( Consumer< IAmmoType > visitor ) {
			this.ammoNBTHandler.forEach( visitor );
		}
		
		@Override
		public IAmmoType peekAmmo() { return this.ammoNBTHandler.peekAmmo(); }
		
		@Override
		public void pushAmmo( IAmmoType ammo )
		{
			this.ammoNBTHandler.pushAmmo( ammo );
			this.syncAndUpdate(); // TODO: Only sync nbt data
		}
		
		@Override
		public IAmmoType popAmmo()
		{
			final IAmmoType ammo = this.ammoNBTHandler.popAmmo();
			this.syncAndUpdate();
			return ammo;
		}
		
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
			
			final boolean isListAmmo = nbt.hasKey( LIST_AMMO_TAG );
			this.ammoNBTHandler = isListAmmo ? new ListAmmoNBTHandler() : new CountAmmoNBTHandler();
		}
		
		protected class ListAmmoNBTHandler implements IAmmoNBTHandler
		{
			protected final LinkedList< IAmmoType > ammoList;
			protected int countAmmoDataSize = 0;
			
			protected ListAmmoNBTHandler()
			{
				this.ammoList = new LinkedList<>();
				final int[] data = Mag.this.nbt.getIntArray( LIST_AMMO_TAG );
				final boolean isOddCount = data.length > 0 && data[ data.length - 1 ] >>> 16 == 0;
				final int size = 2 * data.length - ( isOddCount ? 1 : 0 );
				
				IAmmoType prevAmmo = null;
				for ( int i = 0; i < size; ++i )
				{
					final IAmmoType thisAmmo = this.getAmmo( data, i );
					this.ammoList.add( thisAmmo );
					if ( thisAmmo != prevAmmo )
					{
						prevAmmo = thisAmmo;
						++this.countAmmoDataSize;
					}
				}
			}
			
			protected ListAmmoNBTHandler( LinkedList< IAmmoType > ammoList, int countAmmoDataSize )
			{
				this.countAmmoDataSize = countAmmoDataSize;
				this.ammoList = ammoList;
				final int[] data = new int[ ( ammoList.size() + 1 ) / 2 ];
				
				int i = 0;
				for ( IAmmoType ammo : ammoList )
				{
					this.setAmmo( data, i, Item.getIdFromItem( ammo.item() ) );
					++i;
				}
				
				Mag.this.nbt.setIntArray( LIST_AMMO_TAG, data );
			}
			
			@Override
			public int ammoCount() { return this.ammoList.size(); }
			
			@Override
			public void forEach( Consumer< IAmmoType > visitor )
			{
				final Iterator< IAmmoType > itr = this.ammoList.descendingIterator();
				while ( itr.hasNext() ) { visitor.accept( itr.next() ); }
			}
			
			@Override
			public IAmmoType peekAmmo() { return this.ammoList.getLast(); }
			
			@Override
			public void pushAmmo( IAmmoType ammo )
			{
				final int ammoSize = this.ammoList.size();
				final int lastAmmoId = ammoSize > 0
					? Item.getIdFromItem( this.ammoList.getLast().item() ) : 0;
				final int ammoId = Item.getIdFromItem( ammo.item() );
				this.countAmmoDataSize += ammoId != lastAmmoId ? 1 : 0;
				this.ammoList.addLast( ammo );
				
				final int listAmmoDataSize = ammoSize / 2 + 1;
				if ( this.countAmmoDataSize < listAmmoDataSize )
				{
					final int dataSize = this.countAmmoDataSize;
					Mag.this.ammoNBTHandler = new CountAmmoNBTHandler( this.ammoList, dataSize );
					Mag.this.nbt.removeTag( LIST_AMMO_TAG );
				}
				else
				{
					final int[] data = Mag.this.nbt.getIntArray( LIST_AMMO_TAG );
					
					final boolean needExtension = listAmmoDataSize > data.length;
					if ( needExtension )
					{
						final int[] newArr = new int[ listAmmoDataSize ];
						System.arraycopy( data, 0, newArr, 0, data.length );
						this.setAmmo( newArr, ammoSize, ammoId );
						Mag.this.nbt.setIntArray( LIST_AMMO_TAG, newArr );
					}
					else { this.setAmmo( data, ammoSize, ammoId ); }
				}
			}
			
			@Override
			public IAmmoType popAmmo()
			{
				final int ammoSize = this.ammoList.size();
				final IAmmoType ammo = this.ammoList.removeLast();
				final int ammoId = Item.getIdFromItem( ammo.item() );
				final int lastAmmoId = this.ammoList.size() > 0
					? Item.getIdFromItem( this.ammoList.getLast().item() ) : 0;
				this.countAmmoDataSize -= ammoId != lastAmmoId ? 1 : 0;
				
				final int listAmmoDataSize = ammoSize / 2;
				if ( this.countAmmoDataSize < listAmmoDataSize )
				{
					final int dataSize = this.countAmmoDataSize;
					Mag.this.ammoNBTHandler = new CountAmmoNBTHandler( this.ammoList, dataSize );
					Mag.this.nbt.removeTag( LIST_AMMO_TAG );
				}
				else
				{
					final int[] data = Mag.this.nbt.getIntArray( LIST_AMMO_TAG );
					
					final boolean canShrink = listAmmoDataSize < data.length;
					if ( canShrink )
					{
						final int[] newArr = new int[ listAmmoDataSize ];
						System.arraycopy( data, 0, newArr, 0, listAmmoDataSize );
						Mag.this.nbt.setIntArray( LIST_AMMO_TAG, newArr );
					}
					else { this.setAmmo( data, ammoSize - 1, 0 ); }
				}
				return ammo;
			}
			
			protected IAmmoType getAmmo( int[] data, int idx )
			{
				final boolean isOddIdx = idx % 2 != 0;
				final int offset = isOddIdx ? 16 : 0;
				final int id = 0xFFFF & data[ idx / 2 ] >>> offset;
				final Item item = Item.getItemById( id );
				return ( IAmmoType ) ( ( IItemTypeHost ) item ).meta();
			}
			
			protected void setAmmo( int[] data, int idx, int ammoId )
			{
				final int i = idx / 2;
				final int offset = idx % 2 != 0 ? 16 : 0;
				data[ i ] = data[ i ] & 0xFFFF0000 >>> offset | ammoId << offset;
			}
		}
		
		protected class CountAmmoNBTHandler implements IAmmoNBTHandler
		{
			protected final LinkedList< IAmmoType > ammoList;
			
			protected CountAmmoNBTHandler()
			{
				this.ammoList = new LinkedList<>();
				final int[] data = Mag.this.nbt.getIntArray( COUNT_AMMO_TAG );
				for ( int i = 0; i < data.length; ++i )
				{
					final int value = data[ i ];
					final int ammoId = value >>> 16;
					final int count = 1 + ( value & 0xFFFF );
					
					final Item item = Item.getItemById( ammoId );
					final IAmmoType ammo = ( IAmmoType ) ( ( IItemTypeHost ) item ).meta();
					for ( int j = 0; j < count; ++j ) { this.ammoList.add( ammo ); }
				}
			}
			
			protected CountAmmoNBTHandler( LinkedList< IAmmoType > ammoList, int countAmmoDataSize )
			{
				this.ammoList = ammoList;
				final int[] data = new int[ countAmmoDataSize ];
				
				int i = -1;
				IAmmoType prevAmmo = null;
				for ( IAmmoType ammo : ammoList )
				{
					if ( ammo != prevAmmo )
					{
						++i;
						data[ i ] = Item.getIdFromItem( ammo.item() ) << 16;
						prevAmmo = ammo;
					}
					else { data[ i ] += 1; }
				}
				
				Mag.this.nbt.setIntArray( COUNT_AMMO_TAG, data );
			}
			
			@Override
			public int ammoCount() { return this.ammoList.size(); }
			
			@Override
			public void forEach( Consumer< IAmmoType > visitor )
			{
				final Iterator< IAmmoType > itr = this.ammoList.descendingIterator();
				while ( itr.hasNext() ) { visitor.accept( itr.next() ); }
			}
			
			@Override
			public IAmmoType peekAmmo() { return this.ammoList.getLast(); }
			
			@Override
			public void pushAmmo( IAmmoType ammo )
			{
				final int[] data = Mag.this.nbt.getIntArray( COUNT_AMMO_TAG );
				final int lastAmmoId = data.length > 0 ? data[ data.length - 1 ] >>> 16 : 0;
				final int ammoId = Item.getIdFromItem( ammo.item() );
				final int countAmmoDataSize = data.length + ( ammoId != lastAmmoId ? 1 : 0 );
				
				final int listAmmoDataSize = this.ammoList.size() / 2 + 1;
				this.ammoList.addLast( ammo );
				if ( listAmmoDataSize < countAmmoDataSize )
				{
					final int dataSize = countAmmoDataSize;
					Mag.this.ammoNBTHandler = new ListAmmoNBTHandler( this.ammoList, dataSize );
					Mag.this.nbt.removeTag( COUNT_AMMO_TAG );
				}
				else if ( data.length < countAmmoDataSize )
				{
					final int[] newArr = new int[ countAmmoDataSize ];
					System.arraycopy( data, 0, newArr, 0, data.length );
					newArr[ data.length ] = ammoId << 16;
					Mag.this.nbt.setIntArray( COUNT_AMMO_TAG, newArr );
				}
				else { data[ countAmmoDataSize - 1 ] += 1; }
			}
			
			@Override
			public IAmmoType popAmmo()
			{
				final int[] data = Mag.this.nbt.getIntArray( COUNT_AMMO_TAG );
				final int count = 0xFFFF & data[ data.length - 1 ];
				final int countAmmoDataSize = data.length - ( count > 0 ? 0 : 1 );
				
				final int listAmmoDataSize = this.ammoList.size() / 2;
				final IAmmoType ammo = this.ammoList.removeLast();
				if ( listAmmoDataSize < countAmmoDataSize )
				{
					final int dataSize = countAmmoDataSize;
					Mag.this.ammoNBTHandler = new ListAmmoNBTHandler( this.ammoList, dataSize );
					Mag.this.nbt.removeTag( COUNT_AMMO_TAG );
				}
				else if ( countAmmoDataSize < data.length )
				{
					final int[] newArr = new int[ countAmmoDataSize ];
					System.arraycopy( data, 0, newArr, 0, countAmmoDataSize );
					Mag.this.nbt.setIntArray( COUNT_AMMO_TAG, newArr );
				}
				else { data[ countAmmoDataSize - 1 ] -= 1; }
				return ammo;
			}
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
	
	protected interface IAmmoNBTHandler
	{
		int ammoCount();
		
		void forEach( Consumer< IAmmoType > visitor );

		IAmmoType peekAmmo();
		
		void pushAmmo( IAmmoType ammo );
		
		IAmmoType popAmmo();
	}
}
