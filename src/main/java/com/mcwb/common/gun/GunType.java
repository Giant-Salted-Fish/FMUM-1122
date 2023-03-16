package com.mcwb.common.gun;

import java.util.function.BiConsumer;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import com.google.gson.annotations.SerializedName;
import com.mcwb.client.gun.IGunPartRenderer;
import com.mcwb.client.input.IKeyBind;
import com.mcwb.client.input.Key;
import com.mcwb.client.item.IEquippedItemRenderer;
import com.mcwb.client.item.IItemModel;
import com.mcwb.client.player.OpLoadMagClient;
import com.mcwb.client.player.PlayerPatchClient;
import com.mcwb.client.render.IAnimator;
import com.mcwb.common.load.BuildableLoader;
import com.mcwb.common.load.IContentProvider;
import com.mcwb.common.meta.IMeta;
import com.mcwb.common.module.IModuleEventSubscriber;
import com.mcwb.common.operation.IOperationController;
import com.mcwb.common.operation.OperationController;
import com.mcwb.util.Animation;
import com.mcwb.util.ArmTracker;

import net.minecraft.entity.player.EntityPlayer;
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
	public static final BuildableLoader< IMeta >
		LOADER = new BuildableLoader<>( "gun", JsonGunType.class );
	
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
	
	@Override
	public IMeta build( String name, IContentProvider provider )
	{
		super.build( name, provider );
		
		provider.clientOnly( () -> {
			this.loadMagAnimation = provider.loadAnimation( this.loadMagAnimationPath );
			this.unloadMagAnimation = provider.loadAnimation( this.unloadMagAnimationPath );
		} );
		return this;
	}
	
	@Override
	protected IMeta loader() { return LOADER; }
	
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
				if( gunPart.leftHandPriority() > this.leftHandHolding.leftHandPriority() )
					this.leftHandHolding = gunPart;
				if( gunPart.rightHandPriority() > this.rightHandHolding.rightHandPriority() )
					this.rightHandHolding = gunPart;
			} );
		}
		
		protected class EquippedGun extends EquippedGunPart implements IEquippedGun< C >
		{
			protected EquippedGun(
				Supplier< ER > equippedRenderer,
				EntityPlayer player,
				EnumHand hand
			) { super( equippedRenderer, player, hand ); }
			
			@Override
			@SideOnly( Side.CLIENT )
			public void onKeyPress( IKeyBind key )
			{
				switch( key.name() )
				{
				case Key.LOAD_UNLOAD_MAG:
				case Key.CO_LOAD_UNLOAD_MAG:
					PlayerPatchClient.instance.tryLaunch(
						Gun.this.hasMag()
						? new OpLoadMagClient(
							this,
							GunType.this.loadMagController,
							GunType.this.loadMagAnimation
						)
						: new OpLoadMagClient(
							this,
							GunType.this.loadMagController,
							GunType.this.loadMagAnimation
						)
					);
					break;
					
				default: super.onKeyPress( key );
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
