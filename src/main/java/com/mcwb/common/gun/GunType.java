package com.mcwb.common.gun;

import java.util.function.BiConsumer;

import com.mcwb.client.gun.IGunPartRenderer;
import com.mcwb.client.input.IKeyBind;
import com.mcwb.client.input.InputHandler;
import com.mcwb.client.input.Key;
import com.mcwb.client.player.OpLoadMagClient;
import com.mcwb.client.player.OpModifyClient;
import com.mcwb.client.player.OpUnloadMagClient;
import com.mcwb.client.player.PlayerPatchClient;
import com.mcwb.client.render.IAnimator;
import com.mcwb.common.item.IItem;
import com.mcwb.common.load.BuildableLoader;
import com.mcwb.common.meta.IMeta;
import com.mcwb.common.module.IModular;
import com.mcwb.common.module.IModuleEventSubscriber;
import com.mcwb.common.operation.IOperation;
import com.mcwb.common.operation.IOperationController;
import com.mcwb.common.operation.OperationController;
import com.mcwb.util.ArmTracker;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumHand;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public abstract class GunType< C extends IGun< ? >, R extends IGunPartRenderer< ? super C > >
	extends GunPartType< C, R >
{
	public static final BuildableLoader< IMeta >
		LOADER = new BuildableLoader<>( "gun", GunJson.class );
	
	protected static final OpLoadMagClient OP_LOAD_MAG = new OpLoadMagClient();
	protected static final OpUnloadMagClient OP_UNLOAD_MAG = new OpUnloadMagClient();
	
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
	protected IOperationController unloadMagController = UNLOAD_MAG_CONTROLLER;
	
	@Override
	protected IMeta loader() { return LOADER; }
	
	protected class Gun< T extends IGunPart< ? extends T > >
		extends GunPart< T > implements IGun< T >
	{
		protected transient IGunPart< ? > leftHandHolding = this;
		protected transient IGunPart< ? > rightHandHolding = this;
		
		protected Gun() { }
		
		protected Gun( boolean unused ) { super( unused ); }
		
		public IUseContext onTakeOut( IItem oldItem, EntityPlayer player, EnumHand hand )
		{
			return player.world.isRemote
				? new IUseContext()
				{
					private final IAnimator animator =
						GunType.this.renderer.onTakeOut( Gun.this.self(), hand );
					
					@Override
					public NBTTagCompound data()
					{
						return new NBTTagCompound();
					}
					
					@Override
					@SideOnly( Side.CLIENT )
					public IAnimator animator() { return this.animator; }
				}
				: () -> new NBTTagCompound();
		}
		
		@Override
		public void updateState( BiConsumer< Class< ? >, IModuleEventSubscriber< ? > > registry )
		{
			super.updateState( registry );
			
			this.leftHandHolding = this; // TODO: Validate if necessary
			this.rightHandHolding = this;
			this.forEach( gunPart -> {
				if( gunPart.leftHandPriority() > this.leftHandHolding.leftHandPriority() )
					this.leftHandHolding = gunPart;
				if( gunPart.rightHandPriority() > this.rightHandHolding.rightHandPriority() )
					this.rightHandHolding = gunPart;
			} );
		}
		
		@Override
		public IOperationController loadMagController() { return GunType.this.loadMagController; }
		
		@Override
		public IOperationController unloadMagController() {
			return GunType.this.unloadMagController;
		}
		
		@Override
		@SideOnly( Side.CLIENT )
		public void onKeyPress( IKeyBind key )
		{
			switch( key.name() )
			{
			case Key.LOAD_UNLOAD_MAG:
			case Key.CO_LOAD_UNLOAD_MAG:
				final IOperation op = this.hasMag()
					? OP_UNLOAD_MAG.reset( this ) : OP_LOAD_MAG.reset( this );
				PlayerPatchClient.instance.tryLaunch( op );
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
			final IOperation executing = PlayerPatchClient.instance.executing();
			final boolean modifying = executing instanceof OpModifyClient;
			final boolean freeView = InputHandler.FREE_VIEW.down || InputHandler.CO_FREE_VIEW.down;
			return !( modifying && freeView );
		}
		
		@Override
		@SideOnly( Side.CLIENT )
		public void setupRenderArm( ArmTracker leftArm, ArmTracker rightArm, IAnimator animator )
		{
			this.leftHandHolding.setupLeftArmToRender( leftArm, animator );
			this.rightHandHolding.setupRightArmToRender( rightArm, animator );
		}
	}
	
	protected static class GunWrapper<
		M extends IGunPart< ? extends M >,
		T extends IGun< ? extends M >
	> extends GunPartWrapper< M, T > implements IGun< M >
	{
		protected GunWrapper( T primary, ItemStack stack ) { super( primary, stack ); }
		
		@Override
		public boolean hasMag() { return this.primary.hasMag(); }
		
		@Override
		public boolean isAllowed( IMag< ? > mag ) { return this.primary.isAllowed( mag ); }
		
		@Override
		public void loadMag( IMag< ? > mag ) { this.primary.loadMag( mag ); }
		
		@Override
		public IMag< ? > unloadMag() { return this.primary.unloadMag(); }
		
		@Override
		public IOperationController loadMagController() { return this.primary.loadMagController(); }
		
		@Override
		public IOperationController unloadMagController() {
			return this.primary.unloadMagController();
		}
		
		@Override
		@SideOnly( Side.CLIENT )
		public void setupRenderArm( ArmTracker leftArm, ArmTracker rightArm, IAnimator animator ) {
			throw new RuntimeException();
		}
	}
	
	private static class GunJson extends GunType< IGun< ? >, IGunPartRenderer< ? super IGun< ? > > >
	{
		@Override
		public IModular< ? > newRawContexted()
		{
			return this.new Gun< IGunPart< ? > >()
			{
				// Override this so that we do not need to create a wrapper for it
				@Override
				public void syncAndUpdate() { }
			};
		}
		
		@Override
		public IModular< ? > deserializeContexted( NBTTagCompound nbt )
		{
			final Gun< ? > gun = this.new Gun<>( false );
			gun.deserializeNBT( nbt );
			return gun;
		}
		
		@Override
		protected ICapabilityProvider newWrapper( IGun< ? > primary, ItemStack stack ) {
			return new GunWrapper<>( primary, stack );
		}
	}
}
