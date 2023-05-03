package com.fmum.common.gun;

import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import com.fmum.client.FMUMClient;
import com.fmum.client.camera.ICameraController;
import com.fmum.client.gun.IEquippedGunPartRenderer;
import com.fmum.client.gun.IGunPartRenderer;
import com.fmum.client.input.IInput;
import com.fmum.client.input.Key;
import com.fmum.client.item.IItemModel;
import com.fmum.client.player.OpLoadMagClient;
import com.fmum.client.player.OpUnloadMagClient;
import com.fmum.client.player.OperationClient;
import com.fmum.client.player.PlayerPatchClient;
import com.fmum.client.render.IAnimator;
import com.fmum.common.ammo.IAmmoType;
import com.fmum.common.item.IItemTypeHost;
import com.fmum.common.load.IContentProvider;
import com.fmum.common.meta.IMeta;
import com.fmum.common.module.IModuleEventSubscriber;
import com.fmum.common.network.PacketNotifyItem;
import com.fmum.common.operation.IOperation;
import com.fmum.common.operation.IOperationController;
import com.fmum.common.operation.OperationController;
import com.fmum.common.player.OpLoadMag;
import com.fmum.common.player.OpUnloadMag;
import com.fmum.common.player.PlayerPatch;
import com.fmum.util.Animation;
import com.fmum.util.ArmTracker;
import com.google.gson.annotations.SerializedName;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public abstract class GunType<
	I extends IGunPart< ? extends I >,
	C extends IGun< ? >,
	E extends IEquippedGun< ? extends C >,
	ER extends IEquippedGunPartRenderer< ? super E >,
	R extends IGunPartRenderer< ? super C, ? extends ER >,
	M extends IItemModel< ? extends R >
> extends GunPartType< I, C, E, ER, R, M >
{
	protected static final OperationController
		LOAD_MAG_CONTROLLER = new OperationController(
			1F / 40F,
			new float[] { 0.8F },
			OperationController.NO_SPECIFIED_EFFECT,
			new float[] { 0.8F },
			"load_mag"
		),
		UNLOAD_MAG_CONTROLLER = new OperationController(
			1F / 40F,
			new float[] { 0.5F },
			OperationController.NO_SPECIFIED_EFFECT,
			new float[] { 0.5F },
			"unload_mag"
		),
		CHARGE_GUN_CONTROLLER = new OperationController(
			1F / 22F,
			new float[] { 0.5F },
			OperationController.NO_SPECIFIED_EFFECT,
			OperationController.NO_KEY_TIME
		),
		INSPECT_CONTROLLER = new OperationController( 1F / 40F );
	
	@SerializedName( value = "isOpenBolt", alternate = "openBolt" )
	protected boolean isOpenBolt = false;
	
	protected boolean catchBoltOnEmpty = false;
	
	protected IOperationController
		loadMagController   = LOAD_MAG_CONTROLLER,
		unloadMagController = UNLOAD_MAG_CONTROLLER,
		chargeGunController = CHARGE_GUN_CONTROLLER,
		inspectController   = INSPECT_CONTROLLER;
	
	@Override
	public IMeta build( String name, IContentProvider provider )
	{
		super.build( name, provider );
		
		provider.clientOnly( () -> {
			this.loadMagController.loadAnimation( provider );
			this.unloadMagController.loadAnimation( provider );
			this.chargeGunController.loadAnimation( provider );
			this.inspectController.loadAnimation( provider );
		} );
		return this;
	}
	
	protected abstract class Gun extends GunPart implements IGun< I >
	{
		protected transient IGunPart< ? > leftHandHolding = this;
		protected transient IGunPart< ? > rightHandHolding = this;
		
		protected IGunState state = this.createState();
		
		protected IGunState createState() {
			return GunType.this.isOpenBolt ? this.new StateCloseBolt() : this.new StateBoltRelease();
		}
		
		protected Gun() { }
		
		protected Gun( boolean unused ) { super( unused ); }
		
		@Override
		public boolean hasMag() { return this.getInstalledCount( 0 ) > 0; }
		
		@Nullable
		@Override
		public IMag< ? > mag() {
			return this.hasMag() ? ( IMag< ? > ) this.getInstalled( 0, 0 ) : null;
		}
		
		@Override
		public boolean isAllowed( IMag< ? > mag ) {
			return GunType.this.slots.get( 0 ).isAllowed( mag );
		}
		
		@Override
		public void loadMag( IMag< ? > mag ) { this.install( 0, mag ); }
		
		@Override
		public IMag< ? > unloadMag() { return ( IMag< ? > ) this.remove( 0, 0 ); }
		
		@Override
		public void chargeGun()
		{
			
		}
		
		@Override
		public void updateState( BiConsumer< Class< ? >, IModuleEventSubscriber< ? > > registry )
		{
			super.updateState( registry );
			
			this.leftHandHolding = this;
			this.rightHandHolding = this;
			this.forEach( gunPart -> {
				if ( gunPart.leftHandPriority() > this.leftHandHolding.leftHandPriority() ) {
					this.leftHandHolding = gunPart;
				}
				if ( gunPart.rightHandPriority() > this.rightHandHolding.rightHandPriority() ) {
					this.rightHandHolding = gunPart;
				}
			} );
		}
		
		protected class StateOpenBolt implements IGunState
		{
			@Override
			public IGunState charge( EntityPlayer player ) { return this; }
		}
		
		protected class StateCloseBolt implements IGunState
		{
			@Override
			public IGunState charge( EntityPlayer player ) {
				return Gun.this.new StateOpenBolt();
			}
		}
		
		protected class StateBoltRelease implements IGunState
		{
			@Override
			public IGunState charge( EntityPlayer player )
			{
				final IMag< ? > mag = Gun.this.mag();
				final boolean noMag = mag == null;
				if ( noMag ) { return this; }
				
				final boolean hasAmmo = !mag.isEmpty();
				if ( hasAmmo ) { return Gun.this.new StateShootReady( mag.popAmmo() ); }
				
				return GunType.this.catchBoltOnEmpty ? Gun.this.new StateBoltCatch() : this;
			}
		}
		
		protected class StateBoltCatch implements IGunState
		{
			@Override
			public IGunState charge( EntityPlayer player )
			{
				final IMag< ? > mag = Gun.this.mag();
				final boolean noMag = mag == null;
				if ( noMag ) { return Gun.this.new StateBoltRelease(); }
				
				final boolean hasAmmo = !mag.isEmpty();
				return hasAmmo ? Gun.this.new StateShootReady( mag.popAmmo() ) : this;
			}
		}
		
		protected class StateShootReady implements IGunState
		{
			protected IAmmoType ammoInChamber;
			
			protected StateShootReady( IAmmoType ammo ) { this.ammoInChamber = ammo; }
			
			@Override
			public IGunState charge( EntityPlayer player )
			{
				if ( this.ammoInChamber != null )
				{
					// Eject it.
					final int amount = 1;
					player.dropItem( this.ammoInChamber.item(), amount );
				}
				
				final IMag< ? > mag = Gun.this.mag();
				final boolean noMag = mag == null;
				if ( noMag ) { return Gun.this.new StateBoltRelease(); }
				
				final boolean hasAmmo = !mag.isEmpty();
				if ( hasAmmo )
				{
					this.ammoInChamber = mag.popAmmo();
					return this;
				}
				
				return(
					GunType.this.catchBoltOnEmpty
					? Gun.this.new StateBoltCatch()
					: Gun.this.new StateBoltRelease()
				);
			}
		}
		
		protected class EquippedGun extends EquippedGunPart implements IEquippedGun< C >
		{
			protected static final byte
				OP_CODE_LOAD_MAG = 0,
				OP_CODE_UNLOAD_MAG = 1;
			
			protected EquippedGun(
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
				case OP_CODE_LOAD_MAG:
					final int invSlot = buf.readByte();
					final IOperationController controller0 = GunType.this.loadMagController;
					final IOperation op0 = new OpLoadMag( Gun.this, controller0, invSlot );
					PlayerPatch.get( player ).launch( op0 );
				break;
					
				case OP_CODE_UNLOAD_MAG:
					final IOperationController controller1 = GunType.this.unloadMagController;
					final IOperation op1 = new OpUnloadMag( Gun.this, controller1 );
					PlayerPatch.get( player ).launch( op1 );
				break;
				}
			}
			
			@Override
			@SideOnly( Side.CLIENT )
			@SuppressWarnings( "unchecked" )
			protected void setupInputCallbacks( Map< IInput, Runnable > registry )
			{
				super.setupInputCallbacks( registry );
				
				final Runnable loadUnloadMag = () -> PlayerPatchClient.instance.launch(
					Gun.this.hasMag()
					? new OpUnloadMagClient( this, GunType.this.unloadMagController ) {
						@Override
						protected void launchCallback()
						{
							EquippedGun.this.renderer.useAnimation( this.controller.animation() );
							EquippedGun.this.renderDelegate = ori -> ( E ) EquippedGun.this;
							
							final ICameraController camera = PlayerPatchClient.instance.camera;
							camera.useAnimation( EquippedGun.this.animator() );
							
							this.sendPacketToServer( new PacketNotifyItem(
								buf -> buf.writeByte( OP_CODE_UNLOAD_MAG )
							) );
						}
						
						@Override
						protected void endCallback()
						{
							final EquippedGun equipped = ( EquippedGun ) this.equipped;
							equipped.renderDelegate = original -> original;
							equipped.renderer.useAnimation( Animation.NONE );
						}
					}
					: new OpLoadMagClient( this, GunType.this.loadMagController ) {
						@Override
						protected void launchCallback()
						{
							EquippedGun.this.renderer.useAnimation( this.controller.animation() );
							
							final ICameraController camera = PlayerPatchClient.instance.camera;
							camera.useAnimation( EquippedGun.this.animator() );
							
							// Copy this gun to install the loading mag.
							final EquippedGun copied = ( EquippedGun ) EquippedGun.this.copy();
							final C copiedGun = copied.item();
							
							// Install the loading mag to render it. Copy before use.
							final InventoryPlayer inv = FMUMClient.MC.player.inventory;
							final ItemStack stack = inv.getStackInSlot( this.invSlot ).copy();
							final IMag< ? > mag = ( IMag< ? > ) IItemTypeHost.getItem( stack );
							copiedGun.loadMag( mag );
							mag.setAsLoadingMag();
							
							// Delegate render to copied gun.
							EquippedGun.this.renderDelegate = ori -> ( E ) copied;
							
							// Send packet out!
							this.sendPacketToServer( new PacketNotifyItem( buf -> {
								buf.writeByte( OP_CODE_LOAD_MAG );
								buf.writeByte( this.invSlot );
							} ) );
						}
						
						@Override
						protected void endCallback()
						{
							final EquippedGun equipped = ( EquippedGun ) this.equipped;
							equipped.renderDelegate = original -> original;
							equipped.renderer.useAnimation( Animation.NONE );
						}
					}
				);
				registry.put( Key.LOAD_UNLOAD_MAG, loadUnloadMag );
				registry.put( Key.CO_LOAD_UNLOAD_MAG, loadUnloadMag );
				
				final Runnable chargeGun = () -> PlayerPatchClient.instance.launch(
					new OperationClient< IEquippedGun< ? > >( this, GunType.this.chargeGunController )
					{
						@Override
						protected void launchCallback()
						{
							EquippedGun.this.renderer.useAnimation( this.controller.animation() );
							EquippedGun.this.renderDelegate = ori -> ( E ) EquippedGun.this;
							
							final ICameraController camera = PlayerPatchClient.instance.camera;
							camera.useAnimation( EquippedGun.this.animator() );
						}
						
						@Override
						protected void endCallback()
						{
							final EquippedGun equipped = ( EquippedGun ) this.equipped;
							equipped.renderDelegate = original -> original;
							equipped.renderer.useAnimation( Animation.NONE );
						}
					}
				);
				registry.put( Key.CHARGE_GUN, chargeGun );
				registry.put( Key.CO_CHARGE_GUN, chargeGun );
				
				final Runnable inspectWeapon = () -> PlayerPatchClient.instance.launch(
					new OperationClient< IEquippedGun< ? > >( this, GunType.this.inspectController )
					{
						@Override
						protected void launchCallback()
						{
							EquippedGun.this.renderer.useAnimation( this.controller.animation() );
							final ICameraController camera = PlayerPatchClient.instance.camera;
							camera.useAnimation( EquippedGun.this.animator() );
						}
						
						@Override
						protected void endCallback() {
							EquippedGun.this.renderer.useAnimation( Animation.NONE );
						}
					}
				);
				registry.put( Key.INSPECT, inspectWeapon );
				registry.put( Key.CO_INSPECT, inspectWeapon );
			}
			
			@Override
			@SideOnly( Side.CLIENT )
			public boolean updateViewBobbing( boolean original ) { return false; }
			
			@Override
			@SideOnly( Side.CLIENT )
			public boolean hideCrosshair()
			{
//				final IOperation executing = PlayerPatchClient.instance.executing();
//				final boolean modifying = executing instanceof OpModifyClient;
//				final boolean freeView = InputHandler.FREE_VIEW.down || InputHandler.CO_FREE_VIEW.down;
//				return !( modifying && freeView );
				return true;
			}
			
			@Override
			@SideOnly( Side.CLIENT )
			public void setupRenderArm(
				IAnimator animator,
				ArmTracker leftArm,
				ArmTracker rightArm
			) {
				// TODO: Move to this maybe?
				Gun.this.leftHandHolding.setupLeftArmToRender( animator, leftArm );
				Gun.this.rightHandHolding.setupRightArmToRender( animator, rightArm );
			}
		}
	}
	
	protected interface IGunState
	{
		IGunState charge( EntityPlayer player );
	}
}
