package com.mcwb.common.gun;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import com.google.gson.annotations.SerializedName;
import com.mcwb.client.MCWBClient;
import com.mcwb.client.gun.IGunPartRenderer;
import com.mcwb.client.input.IInput;
import com.mcwb.client.input.Key;
import com.mcwb.client.item.IEquippedItemRenderer;
import com.mcwb.client.item.IItemModel;
import com.mcwb.client.player.OpLoadMagClient;
import com.mcwb.client.player.OpUnloadMagClient;
import com.mcwb.client.player.OperationClient;
import com.mcwb.client.player.PlayerPatchClient;
import com.mcwb.client.render.IAnimator;
import com.mcwb.common.item.IItemTypeHost;
import com.mcwb.common.load.IContentProvider;
import com.mcwb.common.meta.IMeta;
import com.mcwb.common.module.IModuleEventSubscriber;
import com.mcwb.common.network.PacketNotifyItem;
import com.mcwb.common.operation.IOperation;
import com.mcwb.common.operation.IOperationController;
import com.mcwb.common.operation.OperationController;
import com.mcwb.common.player.OpLoadMag;
import com.mcwb.common.player.OpUnloadMag;
import com.mcwb.common.player.PlayerPatch;
import com.mcwb.util.Animation;
import com.mcwb.util.ArmTracker;

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
	ER extends IEquippedItemRenderer< ? super E >,
	R extends IGunPartRenderer< ? super C, ? extends ER >,
	M extends IItemModel< ? extends R >
> extends GunPartType< I, C, E, ER, R, M >
{
	protected static final OperationController
		LOAD_MAG_CONTROLLER = new OperationController(
			1F / 40F,
			new float[] { 0.8F },
			new String[ 0 ],
			new float[] { 0.8F },
			"load_mag"
		),
		UNLOAD_MAG_CONTROLLER = new OperationController(
			1F / 40F,
			new float[] { 0.5F },
			new String[ 0 ],
			new float[] { 0.5F },
			"unload_mag"
		),
		CHARGE_GUN_CONTROLLER = new OperationController(
			1F / 22F,
			new float[] { 0.5F },
			new String[ 0 ],
			new float[] { }
		);
	
	protected IOperationController loadMagController = LOAD_MAG_CONTROLLER;
	@SideOnly( Side.CLIENT )
	@SerializedName( value = "loadMagAnimation" )
	protected String loadMagAnimationPath;
	@SideOnly( Side.CLIENT )
	protected transient Animation loadMagAnimation;
	
	protected IOperationController unloadMagController = UNLOAD_MAG_CONTROLLER;
	@SideOnly( Side.CLIENT )
	@SerializedName( value = "unloadMagAnimation" )
	protected String unloadMagAnimationPath;
	@SideOnly( Side.CLIENT )
	protected transient Animation unloadMagAnimation;
	
	protected IOperationController chargeGunController = CHARGE_GUN_CONTROLLER;
	@SideOnly( Side.CLIENT )
	@SerializedName( value = "chargeGunAnimation" )
	protected String chargeGunAnimationPath;
	@SideOnly( Side.CLIENT )
	protected transient Animation chargeGunAnimation;
	
	@Override
	public IMeta build( String name, IContentProvider provider )
	{
		super.build( name, provider );
		
		provider.clientOnly( () -> {
			this.loadMagAnimation = provider.loadAnimation( this.loadMagAnimationPath );
			this.unloadMagAnimation = provider.loadAnimation( this.unloadMagAnimationPath );
			this.chargeGunAnimation = provider.loadAnimation( this.chargeGunAnimationPath );
		} );
		return this;
	}
	
	protected abstract class Gun extends GunPart implements IGun< I >
	{
		protected transient IGunPart< ? > leftHandHolding = this;
		protected transient IGunPart< ? > rightHandHolding = this;
		
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
					final IOperation op0 = new OpLoadMag( this, controller0, invSlot );
					PlayerPatch.get( player ).launch( op0 );
					break;
					
				case OP_CODE_UNLOAD_MAG:
					final IOperationController controller1 = GunType.this.unloadMagController;
					final IOperation op1 = new OpUnloadMag( this, controller1 );
					PlayerPatch.get( player ).launch( op1 );
					break;
				}
			}
			
			@Override
			@SideOnly( Side.CLIENT )
			@SuppressWarnings( "unchecked" )
			public void onKeyPress( IInput key )
			{
				switch ( key.name() )
				{
				case Key.LOAD_UNLOAD_MAG:
				case Key.CO_LOAD_UNLOAD_MAG:
					PlayerPatchClient.instance.launch(
						Gun.this.hasMag()
						? new OpUnloadMagClient( this, GunType.this.unloadMagController )
						{
							@Override
							protected void launchCallback()
							{
								final Animation animation = GunType.this.unloadMagAnimation;
								PlayerPatchClient.instance.camera.useAnimation( animation );
								EquippedGun.this.animator().useAnimation( animation );
								EquippedGun.this.renderDelegate = ori -> ( E ) EquippedGun.this;
								
								this.sendPacketToServer( new PacketNotifyItem(
									buf -> buf.writeByte( OP_CODE_UNLOAD_MAG )
								) );
							}
							
							@Override
							protected void endCallback() {
								( ( EquippedGun ) this.equipped ).renderDelegate = ori -> ori;
							}
						}
						: new OpLoadMagClient( this, GunType.this.loadMagController )
						{
							@Override
							protected void launchCallback()
							{
								final Animation animation = GunType.this.loadMagAnimation;
								PlayerPatchClient.instance.camera.useAnimation( animation );
								EquippedGun.this.animator().useAnimation( animation );
								EquippedGun.this.renderDelegate = ori -> ( E ) EquippedGun.this;
								
								// Install the loading mag to render it
								final InventoryPlayer inv = MCWBClient.MC.player.inventory;
								final ItemStack stack = inv.getStackInSlot( this.invSlot ).copy();
								final IMag< ? > mag = ( IMag< ? > ) IItemTypeHost.getItem( stack );
								Gun.this.install( 0, mag );
								mag.setAsLoadingMag();
								
								this.sendPacketToServer( new PacketNotifyItem( buf -> {
									buf.writeByte( OP_CODE_LOAD_MAG );
									buf.writeByte( this.invSlot );
								} ) );
							}
							
							@Override
							protected void endCallback()
							{
								// If somehow the load is not completed then remove the loading mag
								final boolean magLoaded = this.equipped != EquippedGun.this;
								if ( !magLoaded ) { Gun.this.remove( 0, 0 ); }
								( ( EquippedGun ) this.equipped ).renderDelegate = ori -> ori;
							}
						}
					);
					break;
					
				case Key.CHARGE_GUN:
				case Key.CO_CHARGE_GUN:
					PlayerPatchClient.instance.launch(
						new OperationClient< IEquippedGun< ? > >( this, GunType.this.chargeGunController )
						{
							@Override
							protected void launchCallback()
							{
								final Animation animation = GunType.this.chargeGunAnimation;
								PlayerPatchClient.instance.camera.useAnimation( animation );
								EquippedGun.this.animator().useAnimation( animation );
							}
						}
					);
					break;
					
				default: // TODO: super.onKeyPress( key );
				}
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
}
