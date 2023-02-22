package com.mcwb.common.gun;

import com.mcwb.client.gun.IGunPartRenderer;
import com.mcwb.client.input.IKeyBind;
import com.mcwb.client.input.InputHandler;
import com.mcwb.client.input.Key;
import com.mcwb.client.player.OpLoadMagClient;
import com.mcwb.client.player.OpUnloadMagClient;
import com.mcwb.client.player.PlayerPatchClient;
import com.mcwb.client.render.IAnimator;
import com.mcwb.common.load.BuildableLoader;
import com.mcwb.common.meta.IMeta;
import com.mcwb.common.modify.IModifiable;
import com.mcwb.common.operation.IOperation;
import com.mcwb.common.operation.IOperationController;
import com.mcwb.common.operation.OperationController;
import com.mcwb.util.ArmTracker;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public abstract class GunType< C extends IGunPart, M extends IGunPartRenderer< ? super C > >
	extends GunPartType< C, M >
{
	public static final BuildableLoader< IMeta >
		LOADER = new BuildableLoader<>( "gun", GunJson.class );
	
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
	
	protected static final OpLoadMagClient OP_LOAD_MAG = new OpLoadMagClient();
	protected static final OpUnloadMagClient OP_UNLOAD_MAG = new OpUnloadMagClient();
	
	protected IOperationController loadMagController = LOAD_MAG_CONTROLLER;
	protected IOperationController unloadMagController = UNLOAD_MAG_CONTROLLER;
	
	@Override
	protected IMeta loader() { return LOADER; }
	
	protected class Gun extends GunPart implements IGun
	{
		protected transient IGunPart leftHandHolding = this;
		protected transient IGunPart rightHandHolding = this;
		
		/**
		 * @see GunPart#ContextedGunPart()
		 */
		protected Gun() { }
		
		/**
		 * @see GunPart#GunPart(NBTTagCompound)
		 */
		protected Gun( NBTTagCompound nbt ) { super( nbt ); }
		
		@Override
		public void updatePrimaryState()
		{
			// TODO: maybe use generic to ensure all mods will be GunPart
			this.forEach( mod -> {
				final boolean isGunPart = mod instanceof IGunPart;
				if( !isGunPart ) return;
				
				final IGunPart gunPart = ( IGunPart ) mod;
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
			final boolean modifying = PlayerPatchClient.instance.operating() == OP_MODIFY;
			final boolean freeView = InputHandler.FREE_VIEW.down || InputHandler.CO_FREE_VIEW.down;
			return !( modifying && freeView );
		}
		
		@Override
		public float aimProgress( float smoother ) {
			return InputHandler.AIM_HOLD.down ? 1F : 0F; // TODO
		}
		
		@Override
		@SideOnly( Side.CLIENT )
		public void setupRenderArm( ArmTracker leftArm, ArmTracker rightArm, IAnimator animator )
		{
			this.leftHandHolding.setupLeftArmToRender( leftArm, animator );
			this.rightHandHolding.setupRightArmToRender( rightArm, animator );
		}
	}
	
	public static class GunJson extends GunType< IGun, IGunPartRenderer< ? super IGun > >
	{
		@Override
		public IModifiable newContexted( NBTTagCompound nbt ) { return this.new Gun( nbt ); }
		
		@Override
		public IModifiable deserializeContexted( NBTTagCompound nbt )
		{
			final Gun gun = this.new Gun();
			gun.deserializeNBT( nbt );
			return gun;
		}
	}
}
