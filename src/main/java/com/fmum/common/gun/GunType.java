package com.fmum.common.gun;

import com.fmum.client.FMUMClient;
import com.fmum.client.ModConfigClient;
import com.fmum.client.camera.ICameraController;
import com.fmum.client.gun.IEquippedGunRenderer;
import com.fmum.client.gun.IGunPartRenderer;
import com.fmum.client.input.IInput;
import com.fmum.client.input.Key;
import com.fmum.client.item.IItemModel;
import com.fmum.client.player.OpModifyClient;
import com.fmum.client.player.OperationClient;
import com.fmum.client.player.PlayerPatchClient;
import com.fmum.client.render.CoupledAnimation;
import com.fmum.client.render.IAnimator;
import com.fmum.client.render.ReadOnlyAnimator;
import com.fmum.common.ModConfig;
import com.fmum.common.ammo.IAmmoType;
import com.fmum.common.gun.IFireController.RPMController;
import com.fmum.common.item.IEquippedItem;
import com.fmum.common.item.IItem;
import com.fmum.common.item.IItemTypeHost;
import com.fmum.common.load.IContentProvider;
import com.fmum.common.mag.IMag;
import com.fmum.common.meta.IMeta;
import com.fmum.common.module.IModuleEventSubscriber;
import com.fmum.common.network.PacketNotifyEquipped;
import com.fmum.common.player.IOperation;
import com.fmum.common.player.Operation;
import com.fmum.common.player.OperationController;
import com.fmum.common.player.OperationController.TimedEffect;
import com.fmum.common.player.OperationController.TimedSound;
import com.fmum.common.player.PlayerPatch;
import com.fmum.util.Animation;
import com.fmum.util.ArmTracker;
import com.google.gson.annotations.SerializedName;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.LinkedList;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public abstract class GunType<
	I extends IGunPart< ? extends I >,
	C extends IGun< ? >,
	E extends IEquippedGun< ? extends C >,
	ER extends IEquippedGunRenderer< ? super E >,
	R extends IGunPartRenderer< ? super C, ? extends ER >,
	M extends IItemModel< ? extends R >
> extends GunPartType< I, C, E, ER, R, M >
{
	protected static final ControllerDispatcher CONTROLLER_DISPATCHER = new ControllerDispatcher(
		new GunOpController(
			1F / 40F,
			new TimedEffect[] { new TimedEffect( 0.8F, "" ) },
			new TimedSound[] { },
			false
		)
	);
	
	protected static final IFireController[] FIRE_CONTROLLERS = {
		new RPMController( "full_auto" )
	};
	
	@SerializedName( value = "fireControllers", alternate = "fireModes" )
	protected IFireController[] fireControllers = FIRE_CONTROLLERS;
	
	protected SoundEvent shootSound;
	
	@SerializedName( value = "isOpenBolt", alternate = "openBolt" )
	protected boolean isOpenBolt = false;
	
	protected boolean catchBoltOnEmpty = false;
	
	@SideOnly( Side.CLIENT )
	@SerializedName( value = "staticBoltRelease", alternate = "staticBoltClose" )
	protected Animation staticBoltRelease;
	
	@SideOnly( Side.CLIENT )
	@SerializedName( value = "staticBoltCatch", alternate = "staticBoltOpen" )
	protected Animation staticBoltCatch;
	
	@SideOnly( Side.CLIENT )
	protected Animation shootAnimation;
	
	@SideOnly( Side.CLIENT )
	protected Animation shootBoltCatchAnimation;
	
	protected ControllerDispatcher
		loadMagControllerDispatcher = CONTROLLER_DISPATCHER,
		unloadMagControllerDispatcher = CONTROLLER_DISPATCHER,
		chargeGunControllerDispatcher = CONTROLLER_DISPATCHER,
		releaseBoltControllerDispatcher = CONTROLLER_DISPATCHER,
		inspectControllerDispatcher = CONTROLLER_DISPATCHER,
		switchFireModeControllerDispatcher = CONTROLLER_DISPATCHER;
	
	@Override
	public IMeta build( String name, IContentProvider provider )
	{
		super.build( name, provider );
		
		provider.clientOnly( () -> {
			this.loadMagControllerDispatcher.checkAssetsSetup( provider );
			this.unloadMagControllerDispatcher.checkAssetsSetup( provider );
			this.chargeGunControllerDispatcher.checkAssetsSetup( provider );
			this.releaseBoltControllerDispatcher.checkAssetsSetup( provider );
			this.inspectControllerDispatcher.checkAssetsSetup( provider );
			this.switchFireModeControllerDispatcher.checkAssetsSetup( provider );
			
			this.staticBoltRelease = Optional.ofNullable( this.staticBoltRelease ).orElse( Animation.NONE );
			this.staticBoltCatch = Optional.ofNullable( this.staticBoltCatch ).orElse( Animation.NONE );
			this.shootAnimation = Optional.ofNullable( this.shootAnimation ).orElse( Animation.NONE );
			this.shootBoltCatchAnimation = Optional.ofNullable( this.shootBoltCatchAnimation ).orElse( Animation.NONE );
		} );
		return this;
	}
	
	protected abstract class Gun extends GunPart implements IGun< I >
	{
		protected transient IGunPart< ? > leftHandHolding = this;
		protected transient IGunPart< ? > rightHandHolding = this;
		
		protected GunState state;
		
		protected IFireController fireController;
		protected int shotCount;
		
		protected Gun()
		{
			this.state = this.createGunState();
			this.fireController = GunType.this.fireControllers[ 0 ];
			
			final int[] data = Gun.this.nbt.getIntArray( DATA_TAG );
			final int baseIdx = super.dataSize();
			data[ baseIdx + 0 ] = this.state.toAmmoIdAndOrdinal();
			
			final int roundsShot = 0;
			data[ baseIdx + 1 ] = roundsShot;
		}
		
		protected Gun( boolean ignoredWaitForDeserialize ) { super( ignoredWaitForDeserialize ); }
		
		@Override
		public boolean hasMag() { return this.getInstalledCount( 0 ) > 0; }
		
		@Nullable
		@Override
		public IMag< ? > mag() {
			return this.hasMag() ? (com.fmum.common.mag.IMag< ? > ) this.getInstalled( 0, 0 ) : null;
		}
		
		@Override
		public boolean isAllowed( IMag< ? > mag ) {
			return GunType.this.slots.get( 0 ).isAllowed( mag );
		}
		
		@Override
		public void loadMag( IMag< ? > mag ) { this.install( 0, mag ); }
		
		@Override
		public IMag< ? > unloadMag() { return (com.fmum.common.mag.IMag< ? > ) this.remove( 0, 0 ); }
		
		@Override
		public void switchFireMode( EntityPlayer player )
		{
			final int[] data = this.nbt.getIntArray( DATA_TAG );
			final int dataIdx = super.dataSize() + 1;
			final int val = data[ dataIdx ];
			final int prevIdx = 0xFFFF & val;
			final int newIdx = ( prevIdx + 1 ) % GunType.this.fireControllers.length;
			this.fireController = GunType.this.fireControllers[ newIdx ];
			data[ dataIdx ] = 0xFFFF0000 & val | newIdx;
			this.syncAndUpdate();
			
			player.sendMessage( new TextComponentTranslation( this.fireController.promptMsg() ) );
		}
		
		@Override
		public void chargeGun( EntityPlayer player )
		{
			this.state = this.state.charge( player );
			this.syncGunState();
		}
		
		@Override
		public void releaseBolt( EntityPlayer player )
		{
			this.state = this.state.releaseBolt( player );
			this.syncGunState();
		}
		
		@Override
		public void updateModuleState(
			BiConsumer< Class< ? >, IModuleEventSubscriber< ? > > registry
		) {
			super.updateModuleState( registry );
			
			this.leftHandHolding = this;
			this.rightHandHolding = this;
			this.forEachModule( gunPart -> {
				if ( gunPart.leftHandPriority() > this.leftHandHolding.leftHandPriority() ) {
					this.leftHandHolding = gunPart;
				}
				if ( gunPart.rightHandPriority() > this.rightHandHolding.rightHandPriority() ) {
					this.rightHandHolding = gunPart;
				}
			} );
		}
		
		@Override
		public void forEachAmmo( Consumer< IAmmoType > visitor ) {
			this.state.forEachAmmo( visitor );
		}
		
		@Override
		public void deserializeNBT( NBTTagCompound nbt )
		{
			super.deserializeNBT( nbt );
			
			final int baseIdx = super.dataSize();
			
			// Weapon state and ammo in barrel.
			final int[] data = Gun.this.nbt.getIntArray( DATA_TAG );
			final int value = data[ baseIdx + 0 ];
			final int stateOrdinary = 0xFFFF & value;
			switch ( stateOrdinary )
			{
//			TODO: case StateBoltOpen.ORDINAL:
//				this.state = new StateBoltOpen();
//				break;
//
//			case StateBoltClose.ORDINAL:
//				this.state = new StateBoltClose();
//				break;
			
			case StateBoltReleaseHammerRelease.ORDINAL:
				this.state = new StateBoltReleaseHammerRelease();
				break;
				
			case StateBoltRealseHammerReady.ORDINAL:
				this.state = new StateBoltRealseHammerReady();
				break;
				
			case StateBoltCatch.ORDINAL:
				this.state = new StateBoltCatch();
				break;
				
			case StateAmmoReadyHammerRelease.ORDINAL: {
					final IAmmoType ammo = IAmmoType.REGISTRY.get( value >>> 16 );
					this.state = new StateAmmoReadyHammerRelease( ammo );
				}
				break;
				
			case StateShootReady.ORDINAL: {
					final IAmmoType ammo = IAmmoType.REGISTRY.get( value >>> 16 );
					this.state = new StateShootReady( ammo );
				}
				break;
			}
			
			// Rounds shot and fire controller index.
			final int val = data[ baseIdx + 1 ];
			this.shotCount = val >>> 16;
			this.fireController = GunType.this.fireControllers[ 0xFFFF & val ];
		}
		
		/**
		 * <pre> 16-bit ammo id     | 16-bit state; </pre>
		 * <pre> 16-bit rounds shot | 16-bit fire controller index; </pre>
		 */
		@Override
		protected int dataSize() { return super.dataSize() + 2; }
		
		protected GunState createGunState() {
			return new StateBoltReleaseHammerRelease(); // TODO: GunType.this.isOpenBolt ? new StateBoltClose() : new StateBoltRelease();
		}
		
		protected final void syncGunState()
		{
			final int[] data = Gun.this.nbt.getIntArray( DATA_TAG );
			data[ super.dataSize() ] = this.state.toAmmoIdAndOrdinal();
			this.syncAndUpdate();
		}
		
		protected GunOpController loadMagController()
		{
			final boolean boltCatch = this.state.isBoltCatch();
			final boolean hammerReady = Gun.this.state.isHammerReady();
			return GunType.this.loadMagControllerDispatcher
		   		.match( this.mag(), boltCatch, boltCatch, hammerReady );
		}
		
		protected GunOpController unloadMagController()
		{
			final boolean boltCatch = this.state.isBoltCatch();
			final boolean hammerReady = Gun.this.state.isHammerReady();
			return GunType.this.unloadMagControllerDispatcher
		   		.match( this.mag(), boltCatch, boltCatch, hammerReady );
		}
		
		protected boolean isCapableToShoot() { return true; }
		
		protected boolean isCapableToCharge() { return true; }
		
		protected boolean isCapableToAuto() { return true; }
		
		protected class EquippedGun extends EquippedGunPart implements IEquippedGun< C >
		{
			protected static final byte
				OP_CODE_LOAD_MAG = 0,
				OP_CODE_UNLOAD_MAG = 1,
				OP_CODE_CHARGE_GUN = 2,
				OP_RELEASE_BOLT = 3,
				OP_SWITCH_FIRE_MODE = 4,
				OP_SHOOT = 5;
			
			protected int actionCoolDown = 0;
			
			protected ITriggerHandler triggerHandler = ITriggerHandler.NONE;
			
			protected EquippedGun( EntityPlayer player, EnumHand hand )
			{
				super( player, hand );
				
				if ( player.world.isRemote )
				{
					this.renderer.useGunAnimation(
						new CoupledAnimation( Gun.this.state.staticAnimation() )
					);
				}
			}
			
			@SuppressWarnings( "unchecked" )
			protected EquippedGun(
				IEquippedItem< ? > prevEquipped,
				EntityPlayer player,
				EnumHand hand
			) {
				super( prevEquipped, player, hand );
				
				final EquippedGun prev = ( EquippedGun ) prevEquipped;
				
				this.actionCoolDown = prev.actionCoolDown;
				if ( player.world.isRemote ) {
					this.triggerHandler = prev.triggerHandler;
				}
			}
			
			@Override
			public void tickInHand( EntityPlayer player, EnumHand hand )
			{
				super.tickInHand( player, hand );
				
				this.triggerHandler = this.triggerHandler.tick( player );
			}
			
			@Override
			public void handlePacket( ByteBuf buf, EntityPlayer player )
			{
				switch ( buf.readByte() )
				{
				case OP_CODE_CHARGE_GUN:
					PlayerPatch.get( player ).launch( new OpChargeGun() );
					break;
					
				case OP_RELEASE_BOLT:
					PlayerPatch.get( player ).launch( new OpReleaseBolt() );
					break;
					
				case OP_CODE_UNLOAD_MAG:
					PlayerPatch.get( player ).launch( new OpUnloadMag() );
					break;
					
				case OP_SWITCH_FIRE_MODE:
					final boolean boltCatch = Gun.this.state.isBoltCatch();
					final boolean hammerReady = Gun.this.state.isHammerReady();
					PlayerPatch.get( player ).launch( new OperationOnGun(
						GunType.this.switchFireModeControllerDispatcher
							.match( Gun.this.mag(), boltCatch, boltCatch, hammerReady )
					) {
						@Override
						protected void doHandleEffect( EntityPlayer player ) {
							this.gun.switchFireMode( player );
						}
					} );
					break;
					
				case OP_CODE_LOAD_MAG:
					final int magInvSlot = buf.readByte();
					PlayerPatch.get( player ).launch( new OpLoadMag( magInvSlot ) );
					break;
					
				case OP_SHOOT:
					final int actionRounds = Gun.this.fireController.actionRounds();
					final boolean safetyOff = actionRounds > 0;
					if ( safetyOff )
					{
						this.triggerHandler = this.triggerHandler
							.enqueueShotRequest( buf, this::serverShootHandler );
					}
					break;
				}
			}
			
			protected ITriggerHandler serverShootHandler( ByteBuf data )
			{
				return new ITriggerHandler() {
					private int coolDownTicks = EquippedGun.this.triggerHandler.coolDownTicks();
					private int timeoutTicks = -ModConfig.shootRequestTimeoutTicks;
					private final LinkedList< Object > shotRequests = new LinkedList<>(); {
						this.shotRequests.add( new Object() );
					}
					
					private int actionRounds = Gun.this.fireController.actionRounds();
					private int actedRounds = 0;
					
					@Override
					public ITriggerHandler enqueueShotRequest(
						ByteBuf requestData,
						Function< ByteBuf, ITriggerHandler > handlerInitializer
					) {
						// TODO: Read shoot params.
						this.shotRequests.add( new Object() );
						return this;
					}
					
					@Override
					public ITriggerHandler tick( EntityPlayer player )
					{
						while ( this.coolDownTicks <= 0 && this.shotRequests.size() > 0 )
						{
							final Object shotRequest = this.shotRequests.poll();
							// TODO: Do shot.
							
							this.actedRounds += 1;
							this.coolDownTicks = Gun.this.fireController
						 		.getCoolDownTicks( Gun.this.shotCount, this.actedRounds, rpm -> rpm );
							this.timeoutTicks = -this.coolDownTicks;
							
							player.sendMessage( new TextComponentString( "C: " + ( Gun.this.shotCount - 1 ) + ", " + this.coolDownTicks ) );
							
							final boolean roundsCompleted = this.actedRounds >= this.actionRounds;
							if ( roundsCompleted ) {
								return new ShotCoolDownCounter( this.coolDownTicks );
							}
						}
						
						final boolean shotTimeOut = this.coolDownTicks <= this.timeoutTicks;
						if ( shotTimeOut ) { return NONE; }
						
						this.coolDownTicks -= 1;
						return this;
					}
					
					@Override
					public int coolDownTicks() { return this.coolDownTicks; }
				};
			}
			
			@Override
			@SideOnly( Side.CLIENT )
			@SuppressWarnings( "unchecked" )
			protected void setupInputHandler(
				BiConsumer< Object, Consumer< IInput > > pressHandlerRegistry,
				BiConsumer< Object, Consumer< IInput > > releaseHandlerRegistry
			) {
				super.setupInputHandler( pressHandlerRegistry, releaseHandlerRegistry );
				
				final Consumer< IInput > loadUnloadMag = key -> PlayerPatchClient.instance.launch(
					Gun.this.hasMag() ? new OpUnloadMagClient() : new OpLoadMagClient()
				);
				pressHandlerRegistry.accept( Key.LOAD_UNLOAD_MAG, loadUnloadMag );
				pressHandlerRegistry.accept( Key.CO_LOAD_UNLOAD_MAG, loadUnloadMag );
				
				final Consumer< IInput > chargeGun =
					key -> PlayerPatchClient.instance.launch( new OpChargeGunClient() );
				pressHandlerRegistry.accept( Key.CHARGE_GUN, chargeGun );
				pressHandlerRegistry.accept( Key.CO_CHARGE_GUN, chargeGun );
				
				final Consumer< IInput > releaseBolt = key -> {
					if ( !GunType.this.isOpenBolt ) {
						PlayerPatchClient.instance.launch( new OpReleaseBoltClient() );
					}
				};
				pressHandlerRegistry.accept( Key.RELEASE_BOLT, releaseBolt );
				pressHandlerRegistry.accept( Key.CO_RELEASE_BOLT, releaseBolt );
				
				final Consumer< IInput > inspectWeapon = key -> {
					final boolean boltCatch = Gun.this.state.isBoltCatch();
					final boolean hammerReady = Gun.this.state.isHammerReady();
					PlayerPatchClient.instance.launch( new OperationOnGunClient(
						GunType.this.inspectControllerDispatcher
							.match( Gun.this.mag(), boltCatch, boltCatch, hammerReady )
					) );
				};
				pressHandlerRegistry.accept( Key.INSPECT, inspectWeapon );
				pressHandlerRegistry.accept( Key.CO_INSPECT, inspectWeapon );
				
				final Consumer< IInput > switchFireMode = key -> {
					final boolean boltCatch = Gun.this.state.isBoltCatch();
					final boolean hammerReady = Gun.this.state.isHammerReady();
					PlayerPatchClient.instance.launch( new OperationOnGunClient(
						GunType.this.switchFireModeControllerDispatcher
							.match( Gun.this.mag(), boltCatch, boltCatch, hammerReady )
					) {
						@Override
						public IOperation launch( EntityPlayer player )
						{
							FMUMClient.sendPacketToServer(
								new PacketNotifyEquipped( buf -> buf.writeByte( OP_SWITCH_FIRE_MODE ) )
							);
							return this;
						}
					} );
				};
				pressHandlerRegistry.accept( Key.SWITCH_FIRE_MODE, switchFireMode );
				pressHandlerRegistry.accept( Key.CO_SWITCH_FIRE_MODE, switchFireMode );
				
				final Consumer< IInput > pullTrigger = key -> {
					final int actionRounds = Gun.this.fireController.actionRounds();
					final boolean safetyOn = actionRounds <= 0;
					if ( safetyOn ) { return; }
					
					// Delegate render to buffered instance.
					final E copied = this.triggerHandler.getDelegate( () -> ( E ) this.copy() );
					this.renderDelegate = ori -> copied;
					
					// Can shoot, setup trigger handler.
					EquippedGun.this.triggerHandler = new ITriggerHandler() {
						private int actedRounds = 0;
						private int shotCount = EquippedGun.this
							.triggerHandler.getShotCount( Gun.this.shotCount );
						private int coolDownTicks = EquippedGun.this.triggerHandler.coolDownTicks();
						
						@Override
						public ITriggerHandler tick( EntityPlayer player )
						{
							// Keep fire if cool down ticks is 0.
							while ( this.coolDownTicks <= 0 )
							{
								// TODO: Proper sound play.
								player.world.playSound(
									player.posX, player.posY, player.posZ,
									GunType.this.shootSound,
									SoundCategory.PLAYERS,
									1F, 1F, false
								);
								
								// TODO: Update shoot state for copied item.
								FMUMClient.sendPacketToServer(
									new PacketNotifyEquipped( buf -> buf.writeByte( OP_SHOOT ) )
								);
								
								this.actedRounds += 1;
								this.shotCount += 1;
								this.coolDownTicks = Gun.this.fireController
							 		.getCoolDownTicks( this.shotCount, this.actedRounds, rpm -> rpm );
								FMUMClient.sendPlayerMsg( "C: " + ( this.shotCount ) + ", " + this.coolDownTicks );
								
								final boolean roundsCompleted = this.actedRounds >= actionRounds;
								if ( roundsCompleted ) { return this.onTriggerRelease(); }
							}
							
							this.coolDownTicks -= 1;
							return this;
						}
						
						@Override
						public int coolDownTicks() { return this.coolDownTicks; }
						
						@Override
						public ITriggerHandler onTriggerRelease()
						{
							return new ShotSyncWaiter(
								() -> EquippedGun.this.renderDelegate = ori -> ori,
								this.coolDownTicks, this.shotCount, copied
							);
						}
						
						@Override
						public int getShotCount( int rawShotCount ) { return this.shotCount; }
						
						@Override
						public < EQ > EQ getDelegate( Supplier< EQ > supplier ) {
							return ( EQ ) copied;
						}
					};
				};
				pressHandlerRegistry.accept( Key.PULL_TRIGGER, pullTrigger );
				
				releaseHandlerRegistry.accept(
					Key.PULL_TRIGGER,
					key -> this.triggerHandler = this.triggerHandler.onTriggerRelease()
				);
			}
			
			@Override
			@SideOnly( Side.CLIENT )
			public boolean updateViewBobbing( boolean original ) { return false; }
			
			@Override
			@SideOnly( Side.CLIENT )
			public boolean hideCrosshair()
			{
				final IOperation executing = PlayerPatchClient.instance.executing();
				final boolean modifying = executing instanceof OpModifyClient;
				final boolean freeView = Key.FREE_VIEW.down || Key.CO_FREE_VIEW.down;
				return !( modifying && freeView );
			}
			
			@Override
			@SideOnly( Side.CLIENT )
			public void setupRenderArm(
				IAnimator animator,
				ArmTracker leftArm, ArmTracker rightArm
			) {
				// TODO: Move to this maybe?
				Gun.this.leftHandHolding.setupLeftArmToRender( animator, leftArm );
				Gun.this.rightHandHolding.setupRightArmToRender( animator, rightArm );
			}
			
			@SideOnly( Side.CLIENT )
			protected final Gun inner() { return Gun.this; }
			
			@SideOnly( Side.CLIENT )
			protected class OperationOnGunClient
				extends OperationClient< EquippedGun, GunOpController >
			{
				protected OperationOnGunClient( GunOpController controller ) {
					super( EquippedGun.this, controller );
				}
				
				@Override
				@SuppressWarnings( "unchecked" )
				public IOperation launch( EntityPlayer player )
				{
					this.equipped.renderer.useOperateAnimation(
						new CoupledAnimation( this.controller.animation(), this::smoothedProgress )
					);
					this.controller.setupStaticAnimation( EquippedGun.this.renderer::useGunAnimation );
					this.equipped.renderDelegate = ori -> ( E ) EquippedGun.this;
					
					final ICameraController camera = PlayerPatchClient.instance.camera;
					camera.useAnimation( new ReadOnlyAnimator( this.equipped.animator() ) );
					return this;
				}
				
				@Override
				protected void endCallback()
				{
					this.equipped.renderDelegate = original -> original;
					this.equipped.renderer.useOperateAnimation( IAnimator.NONE );
					EquippedGun.this.renderer.useGunAnimation(
						new CoupledAnimation( this.equipped.inner().state.staticAnimation() )
					);
				}
			}
			
			@SideOnly( Side.CLIENT )
			protected class OpUnloadMagClient extends OperationOnGunClient
			{
				protected OpUnloadMagClient() { super( Gun.this.unloadMagController() ); }
				
				@Override
				public IOperation launch( EntityPlayer player )
				{
					FMUMClient.sendPacketToServer(
						new PacketNotifyEquipped( buf -> buf.writeByte( OP_CODE_UNLOAD_MAG ) )
					);
					return super.launch( player );
				}
			}
			
			@SideOnly( Side.CLIENT )
			protected class OpLoadMagClient extends OperationOnGunClient
			{
				protected OpLoadMagClient() { super( Gun.this.loadMagController() ); }
				
				@Override
				@SuppressWarnings( "unchecked" )
				public IOperation launch( EntityPlayer player )
				{
					final InventoryPlayer inv = player.inventory;
					final int invSlot = this.findValidMagInvSlot( inv );
					final boolean noValidMagToLoad = invSlot == -1;
					if ( noValidMagToLoad ) { return IOperation.NONE; }
					
					// Play animation.
					super.launch( player );
					
					// Copy this gun to install the loading mag.
					final EquippedGun copied = ( EquippedGun ) this.equipped.copy();
					final C copiedGun = copied.item();
					
					// Install the loading mag to render it. // Copy before use.
					final ItemStack stack = inv.getStackInSlot( invSlot ).copy();
					final IMag< ? > mag = (com.fmum.common.mag.IMag< ? > ) IItemTypeHost.getItem( stack );
					copiedGun.loadMag( mag );
					mag.setAsLoadingMag();
					
					// Delegate render to copied gun.
					this.equipped.renderDelegate = ori -> ( E ) copied;
					
					// Send out packet!
					FMUMClient.sendPacketToServer( new PacketNotifyEquipped( buf -> {
						buf.writeByte( OP_CODE_LOAD_MAG );
						buf.writeByte( invSlot );
					} ) );
					return this;
				}
				
				protected int findValidMagInvSlot( IInventory inv )
				{
					final int size = inv.getSizeInventory();
					for ( int i = 0; i < size; ++i )
					{
						final ItemStack stack = inv.getStackInSlot( i );
						final IItem item = IItemTypeHost.getItemOrDefault( stack );
						final boolean isMag = item instanceof IMag< ? >;
						final boolean isValidMag = isMag && Gun.this.isAllowed( (com.fmum.common.mag.IMag< ? > ) item );
						if ( isValidMag ) { return i; }
					}
					return -1;
				}
			}
			
			@SideOnly( Side.CLIENT )
			protected class OpChargeGunClient extends OperationOnGunClient
			{
				protected OpChargeGunClient() { super( Gun.this.state.chargeController() ); }
				
				@Override
				public IOperation launch( EntityPlayer player )
				{
					FMUMClient.sendPacketToServer(
						new PacketNotifyEquipped( buf -> buf.writeByte( OP_CODE_CHARGE_GUN ) )
					);
					return super.launch( player );
				}
			}
			
			@SideOnly( Side.CLIENT )
			protected class OpReleaseBoltClient extends OperationOnGunClient
			{
				protected OpReleaseBoltClient() { super( Gun.this.state.releaseBoltController() ); }
				
				@Override
				public IOperation launch( EntityPlayer player )
				{
					FMUMClient.sendPacketToServer(
						new PacketNotifyEquipped( buf -> buf.writeByte( OP_RELEASE_BOLT ) )
					);
					return super.launch( player );
				}
			}
		}
		
//		protected class StateBoltOpen extends GunState
//		{
//			protected static final int ORDINAL = 0;
//
//			@Override
//			protected GunOpController chargeController()
//			{
//				final boolean boltOpenBeforeAction = true;
//				final boolean boltOpenAfterAction = true;
//				return GunType.this.chargeGunControllerDispatcher.match(
//					Gun.this.mag(), boltOpenBeforeAction, boltOpenAfterAction
//				);
//			}
//
//			@Override
//			protected boolean boltCatch() { return true; }
//
//			@Override
//			protected GunState charge( EntityPlayer player ) { return this; }
//
//			@Override
//			protected int toAmmoIdAndOrdinal() { return ORDINAL; }
//
//			@Override
//			protected GunOpController releaseBoltController() { throw new RuntimeException(); }
//
//			@Override
//			protected GunState releaseBolt( EntityPlayer player ) { throw new RuntimeException(); }
//
//			@Override
//			@SideOnly( Side.CLIENT )
//			protected Animation staticAnimation() { return GunType.this.staticBoltCatch; }
//		}
//
//		protected class StateBoltClose extends GunState
//		{
//			protected static final int ORDINAL = 1;
//
//			@Override
//			protected GunOpController chargeController()
//			{
//				final boolean boltOpenBeforeAction = false;
//				final boolean boltOpenAfterAction = true;
//				return GunType.this.chargeGunControllerDispatcher.match(
//					Gun.this.mag(), boltOpenBeforeAction, boltOpenAfterAction
//				);
//			}
//
//			@Override
//			protected GunState charge( EntityPlayer player ) {
//				return new StateBoltOpen(); // TODO: Check if creating inner without Gun.this.new keeps current instance.
//			}
//
//			@Override
//			protected GunOpController releaseBoltController() { throw new RuntimeException(); }
//
//			@Override
//			protected GunState releaseBolt( EntityPlayer player ) { throw new RuntimeException(); }
//
//			@Override
//			protected int toAmmoIdAndOrdinal() { return ORDINAL; }
//
//			@Override
//			@SideOnly( Side.CLIENT )
//			protected Animation staticAnimation() { return GunType.this.staticBoltRelease; }
//		}
		
		protected class StateBoltReleaseHammerRelease extends GunState
		{
			protected static final int ORDINAL = 0;
			
			@Override
			protected boolean isHammerReady() { return false; }
			
			@Override
			protected GunOpController chargeController()
			{
				final IMag< ? > mag = Gun.this.mag();
				final boolean hasMag = mag != null;
				final boolean catchBefore = false;
				final boolean catchAfter = GunType.this.catchBoltOnEmpty && hasMag && mag.isEmpty();
				final boolean hammerReady = false;
				final ControllerDispatcher dispatcher = GunType.this.chargeGunControllerDispatcher;
				return dispatcher.match( mag, catchBefore, catchAfter, hammerReady );
			}
			
			@Override
			protected GunState charge( EntityPlayer player )
			{
				final IMag< ? > mag = Gun.this.mag();
				final boolean noMag = mag == null;
				if ( noMag ) { return new StateBoltRealseHammerReady(); }
				
				final boolean hasAmmo = !mag.isEmpty();
				if ( hasAmmo ) { return new StateShootReady( mag.popAmmo() ); }
				
				return(
					GunType.this.catchBoltOnEmpty
					? new StateBoltCatch()
					: new StateBoltRealseHammerReady()
				);
			}
			
			@Override
			protected GunOpController releaseBoltController()
			{
				final boolean boltCatch = false;
				final boolean hammerReady = false;
				final ControllerDispatcher dispatcher = GunType.this.releaseBoltControllerDispatcher;
				return dispatcher.match( Gun.this.mag(), boltCatch, boltCatch, hammerReady );
			}
			
			@Override
			protected int toAmmoIdAndOrdinal() { return ORDINAL; }
			
			@Override
			@SideOnly( Side.CLIENT )
			protected Animation staticAnimation() { return GunType.this.staticBoltRelease; }
		}
		
		protected class StateBoltRealseHammerReady extends GunState
		{
			protected static final int ORDINAL = 1;
			
			@Override
			protected GunOpController chargeController()
			{
				final IMag< ? > mag = Gun.this.mag();
				final boolean hasMag = mag != null;
				final boolean catchBefore = false;
				final boolean catchAfter = GunType.this.catchBoltOnEmpty && hasMag && mag.isEmpty();
				final boolean hammerReady = true;
				final ControllerDispatcher dispatcher = GunType.this.chargeGunControllerDispatcher;
				return dispatcher.match( mag, catchBefore, catchAfter, hammerReady );
			}
			
			@Override
			protected GunState charge( EntityPlayer player )
			{
				final IMag< ? > mag = Gun.this.mag();
				final boolean noMag = mag == null;
				if ( noMag ) { return this; }
				
				final boolean hasAmmo = !mag.isEmpty();
				if ( hasAmmo ) { return new StateShootReady( mag.popAmmo() ); }
				
				return GunType.this.catchBoltOnEmpty ? new StateBoltCatch() : this;
			}
			
			@Override
			protected GunOpController releaseBoltController()
			{
				final boolean boltCatch = false;
				final boolean hammerReady = true;
				final ControllerDispatcher dispatcher = GunType.this.releaseBoltControllerDispatcher;
				return dispatcher.match( Gun.this.mag(), boltCatch, boltCatch, hammerReady );
			}
			
			@Override
			protected int toAmmoIdAndOrdinal() { return ORDINAL; }
			
			@Override
			@SideOnly( Side.CLIENT )
			protected Animation staticAnimation() { return GunType.this.staticBoltRelease; }
		}
		
		protected class StateAmmoReadyHammerRelease extends StateBoltReleaseHammerRelease
		{
			protected static final int ORDINAL = 2;
			
			protected IAmmoType ammo;
			
			protected StateAmmoReadyHammerRelease( IAmmoType ammo ) { this.ammo = ammo; }
			
			@Override
			protected GunState charge( EntityPlayer player )
			{
				// Eject ammo in chamber.
				final int amount = 1;
				player.dropItem( this.ammo.item(), amount );
				
				final IMag< ? > mag = Gun.this.mag();
				final boolean noMag = mag == null;
				if ( noMag ) { return new StateBoltRealseHammerReady(); }
				
				final boolean notMagEmpty = !mag.isEmpty();
				if ( notMagEmpty ) { return new StateShootReady( mag.popAmmo() ); }
				
				return(
					GunType.this.catchBoltOnEmpty
					? new StateBoltCatch()
					: new StateBoltRealseHammerReady()
				);
			}
			
			@Override
			protected void forEachAmmo( Consumer< IAmmoType > visitor ) {
				visitor.accept( this.ammo );
			}
			
			@Override
			protected int toAmmoIdAndOrdinal() {
				return ORDINAL + ( IAmmoType.REGISTRY.getId( this.ammo ) << 16 );
			}
		}
		
		protected class StateShootReady extends StateBoltRealseHammerReady
		{
			protected static final int ORDINAL = 3;
			
			protected IAmmoType ammo;
			
			protected StateShootReady( IAmmoType ammo ) { this.ammo = ammo; }
			
//			@Override
//			public ShootResult tryShoot( int actedRounds, int shotCount, EntityPlayer player )
//			{
//				player.world.playSound(
//					player.posX, player.posY, player.posZ,
//					GunType.this.shootSound,
//					SoundCategory.PLAYERS,
//					1F, 1F, false
//				);
//				return new ShootResult( newState, actionDuration, actionAnimation, actionRounds, shootCount );
//			}
			
			@Override
			protected GunState charge( EntityPlayer player )
			{
				// Eject ammo in chamber.
				final int amount = 1;
				player.dropItem( this.ammo.item(), amount );
				
				final IMag< ? > mag = Gun.this.mag();
				final boolean noMag = mag == null;
				if ( noMag ) { return new StateBoltRealseHammerReady(); }
				
				final boolean hasAmmoInMag = !mag.isEmpty();
				if ( hasAmmoInMag )
				{
					this.ammo = mag.popAmmo();
					return this;
				}
				
				return(
					GunType.this.catchBoltOnEmpty
					? new StateBoltCatch()
					: new StateBoltRealseHammerReady()
				);
			}
			
			@Override
			protected void forEachAmmo( Consumer< IAmmoType > visitor ) {
				visitor.accept( this.ammo );
			}
			
			@Override
			protected int toAmmoIdAndOrdinal() {
				return ORDINAL + ( IAmmoType.REGISTRY.getId( this.ammo ) << 16 );
			}
		}
		
		protected class StateBoltCatch extends GunState
		{
			protected static final int ORDINAL = 4;
			
			@Override
			protected GunOpController chargeController()
			{
				final IMag< ? > mag = Gun.this.mag();
				final boolean hasMag = mag != null;
				final boolean catchBefore = true;
				final boolean catchAfter = hasMag && mag.isEmpty();
				final boolean hammerReady = true;
				final ControllerDispatcher dispatcher = GunType.this.chargeGunControllerDispatcher;
				return dispatcher.match( mag, catchBefore, catchAfter, hammerReady );
			}
			
			@Override
			protected GunState charge( EntityPlayer player )
			{
				final IMag< ? > mag = Gun.this.mag();
				final boolean noMag = mag == null;
				if ( noMag ) { return new StateBoltRealseHammerReady(); }
				
				final boolean magHasAmmo = !mag.isEmpty();
				return magHasAmmo ? new StateShootReady( mag.popAmmo() ) : this;
			}
			
			@Override
			protected GunOpController releaseBoltController()
			{
				final boolean catchBefore = true;
				final boolean catchAfter = false;
				final boolean hammerReady = false;
				final ControllerDispatcher dispatcher = GunType.this.releaseBoltControllerDispatcher;
				return dispatcher.match( Gun.this.mag(), catchBefore, catchAfter, hammerReady );
			}
			
			@Override
			protected GunState releaseBolt( EntityPlayer player )
			{
				final IMag< ? > mag = Gun.this.mag();
				return(
					mag != null && !mag.isEmpty()
					? new StateShootReady( mag.popAmmo() )
					: new StateBoltRealseHammerReady()
				);
			}
			
			@Override
			protected int toAmmoIdAndOrdinal() { return ORDINAL; }
			
			@Override
			@SideOnly( Side.CLIENT )
			protected Animation staticAnimation() { return GunType.this.staticBoltCatch; }
		}
		
		protected class OperationOnGun extends Operation< OperationController >
		{
			protected OperationOnGun( OperationController controller ) { super( controller ); }
			
			protected IGun< ? > gun = Gun.this;
			
			@Override
			public IOperation onStackUpdate( IEquippedItem< ? > newEquipped, EntityPlayer player )
			{
				this.gun = ( IGun< ? > ) newEquipped.item();
				return this;
			}
		}
		
		protected class OpLoadMag extends OperationOnGun
		{
			protected final int magInvSlot;
			
			protected OpLoadMag( int magInvSlot )
			{
				super( Gun.this.loadMagController() );
				
				this.magInvSlot = magInvSlot;
			}
			
			@Override
			public IOperation launch( EntityPlayer player )
			{
				final boolean alreadyHasMag = this.gun.hasMag();
				if ( alreadyHasMag ) { return IOperation.NONE; }
				
				final ItemStack stack = player.inventory.getStackInSlot( this.magInvSlot );
				final IItem item = IItemTypeHost.getItemOrDefault( stack );
				final boolean isMag = item instanceof IMag< ? >;
				final boolean isValidMag = isMag && this.gun.isAllowed( (com.fmum.common.mag.IMag< ? > ) item );
				return isValidMag ? this : IOperation.NONE;
			}
			
			@Override
			protected void doHandleEffect( EntityPlayer player )
			{
				final InventoryPlayer inv = player.inventory;
				final ItemStack stack = inv.getStackInSlot( this.magInvSlot );
				final IItem item = IItemTypeHost.getItemOrDefault( stack );
				final boolean isMag = item instanceof IMag< ? >;
				if ( !isMag ) { return; }
				
				final IMag< ? > mag = (com.fmum.common.mag.IMag< ? > ) item;
				if ( !this.gun.isAllowed( mag ) ) { return; }
				
				this.gun.loadMag( mag );
				inv.setInventorySlotContents( this.magInvSlot, ItemStack.EMPTY );
			}
		}
		
		protected class OpUnloadMag extends OperationOnGun
		{
			protected OpUnloadMag() { super( Gun.this.unloadMagController() ); }
			
			@Override
			public IOperation launch( EntityPlayer player ) {
				return this.gun.hasMag() ? this : IOperation.NONE;
			}
			
			@Override
			protected void doHandleEffect( EntityPlayer player ) {
				player.addItemStackToInventory( this.gun.unloadMag().toStack() );
			}
		}
		
		protected class OpChargeGun extends OperationOnGun
		{
			protected OpChargeGun() { super( Gun.this.state.chargeController() ); }
			
			@Override
			protected void doHandleEffect( EntityPlayer player ) { this.gun.chargeGun( player ); }
		}
		
		protected class OpReleaseBolt extends OperationOnGun
		{
			protected OpReleaseBolt() { super( Gun.this.state.releaseBoltController() ); }
			
			@Override
			protected void doHandleEffect( EntityPlayer player ) { this.gun.releaseBolt( player ); }
		}
	}
	
	protected static abstract class GunState
	{
		protected boolean isBoltCatch() { return false; }
		
		protected boolean isHammerReady() { return true; }
		
//		protected abstract ShootResult tryShoot( int actedRounds, int shotNumber, EntityPlayer player );
		
		protected abstract GunOpController chargeController();
		
		protected abstract GunState charge( EntityPlayer player );
		
		protected abstract GunOpController releaseBoltController();
		
		protected GunState releaseBolt( EntityPlayer player ) { return this; }
		
		protected void forEachAmmo( Consumer< IAmmoType > visitor ) { }
		
		protected abstract int toAmmoIdAndOrdinal();
		
		@SideOnly( Side.CLIENT )
		protected abstract Animation staticAnimation();
	}
	
	protected interface ITriggerHandler
	{
		ITriggerHandler NONE = new ITriggerHandler()
		{
			@Override
			public ITriggerHandler tick( EntityPlayer player ) { return this; }
			
			@Override
			public int coolDownTicks() { return 0; }
			
			@Override
			public ITriggerHandler enqueueShotRequest(
				ByteBuf requestData,
				Function< ByteBuf, ITriggerHandler > handlerInitializer
			) { return handlerInitializer.apply( requestData ); }
			
			@Override
			@SideOnly( Side.CLIENT )
			public ITriggerHandler onTriggerRelease() { return this; }
			
			@Override
			@SideOnly( Side.CLIENT )
			public int getShotCount( int rawShotCount ) { return rawShotCount; }
			
			@Override
			@SideOnly( Side.CLIENT )
			public < E > E getDelegate( Supplier< E > supplier ) { return supplier.get(); }
		};
		
		ITriggerHandler tick( EntityPlayer player );
		
		int coolDownTicks();
		
		/**
		 * Only used on logical server.
		 */
		default ITriggerHandler enqueueShotRequest(
			ByteBuf requestData,
			Function< ByteBuf, ITriggerHandler > handlerInitializer
		) { throw new RuntimeException(); }
		
		@SideOnly( Side.CLIENT )
		default ITriggerHandler onTriggerRelease() { throw new RuntimeException(); }
		
		@SideOnly( Side.CLIENT )
		default int getShotCount( int rawShotCount ) { throw new RuntimeException(); }
		
		@SideOnly( Side.CLIENT )
		default < E > E getDelegate( Supplier< E > supplier ) { throw new RuntimeException(); }
	}
	
	protected static class ShotCoolDownCounter implements ITriggerHandler
	{
		protected int coolDownTicks;
		
		protected ShotCoolDownCounter( int coolDownTicksLeft ) {
			this.coolDownTicks = coolDownTicksLeft;
		}
		
		@Override
		public ITriggerHandler tick( EntityPlayer player )
		{
			this.coolDownTicks -= 1;
			return this.coolDownTicks > 0 ? this : NONE;
		}
		
		@Override
		public int coolDownTicks() { return this.coolDownTicks; }
		
		@Override
		public ITriggerHandler enqueueShotRequest(
			ByteBuf requestData,
			Function< ByteBuf, ITriggerHandler > handlerInitializer
		) { return handlerInitializer.apply( requestData ); }
	}
	
	@SideOnly( Side.CLIENT )
	protected static class ShotSyncWaiter extends ShotCoolDownCounter
	{
		// TODO: This part is actually client only.
		protected int syncWaitTicks = ModConfigClient.shotSyncWaitTicks;
		protected final Runnable syncFunc;
		
		protected final int bufferedShotCount;
		protected final Object delegate;
		
		protected ShotSyncWaiter(
			Runnable syncFunc,
			int coolDownTicksLeft,
			int bufferedShotCount,
			Object delegate
		) {
			super( coolDownTicksLeft );
			
			this.syncFunc = syncFunc;
			this.bufferedShotCount = bufferedShotCount;
			this.delegate = delegate;
		}
		
		@Override
		public ITriggerHandler tick( EntityPlayer player )
		{
			// Set back render delegate if sync wait time is passed.
			if ( this.syncWaitTicks == 0 ) { this.syncFunc.run(); }
			this.syncWaitTicks -= 1;
			
			this.coolDownTicks -= 1;
			return this.syncWaitTicks < 0 && this.coolDownTicks < 0 ? NONE : this;
		}
		
		@Override
		public ITriggerHandler onTriggerRelease() { return this; }
		
		@Override
		public int getShotCount( int rawShotCount ) { return this.bufferedShotCount; }
		
		@Override
		@SuppressWarnings( "unchecked" )
		public < E > E getDelegate( Supplier< E > supplier ) { return ( E ) this.delegate; }
	}
	
//	protected static class ShootResult
//	{
//		public final IGunState newState;
//		public final int actionDuration;
//		public final Animation actionAnimation;
//		public final int actedRounds;
//		public final int shotCount;
//
//		public ShootResult(
//			IGunState newState,
//			int actionDuration,
//			Animation actionAnimation,
//			int actionRounds,
//			int shotCount
//		) {
//			this.newState = newState;
//			this.actionDuration = actionDuration;
//			this.actionAnimation = actionAnimation;
//			this.actedRounds = actionRounds;
//			this.shotCount = shotCount;
//		}
//	}
}
