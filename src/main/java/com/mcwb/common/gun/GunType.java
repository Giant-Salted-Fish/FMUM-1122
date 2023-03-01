package com.mcwb.common.gun;

import com.mcwb.client.gun.IGunPartRenderer;
import com.mcwb.client.render.IAnimator;
import com.mcwb.common.load.BuildableLoader;
import com.mcwb.common.meta.IMeta;
import com.mcwb.common.module.IModular;
import com.mcwb.util.ArmTracker;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public abstract class GunType< C extends IGunPart< ? >, R extends IGunPartRenderer< ? super C > >
	extends GunPartType< C, R >
{
	public static final BuildableLoader< IMeta >
		LOADER = new BuildableLoader<>( "gun", GunJson.class );
	
	@Override
	protected IMeta loader() { return LOADER; }
	
	protected class Gun< T extends IGunPart< ? extends T > >
		extends GunPart< T > implements IGun< T >
	{
		protected transient IGunPart< ? > leftHandHolding = this;
		protected transient IGunPart< ? > rightHandHolding = this;
		
		protected Gun() { }
		
		protected Gun( NBTTagCompound nbt ) { super( nbt ); }
		
		@Override
		public void updateState()
		{
			super.updateState();
			
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
		@SideOnly( Side.CLIENT )
		public boolean updateViewBobbing( boolean original ) { return false; }
		
		@Override
		@SideOnly( Side.CLIENT )
		public boolean hideCrosshair()
		{
//			final boolean modifying = PlayerPatchClient.instance.executing() == OP_MODIFY;
//			final boolean freeView = InputHandler.FREE_VIEW.down || InputHandler.CO_FREE_VIEW.down;
//			return !( modifying && freeView );
			return true;
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
		protected GunWrapper( IModular< ? > primary, ItemStack stack ) {
			super( primary, stack );
		}
		
		@Override
		@SideOnly( Side.CLIENT )
		public void setupRenderArm( ArmTracker leftArm, ArmTracker rightArm, IAnimator animator ) {
			throw new RuntimeException( "Try to call setup render arm on wrapper" );
		}
	}
	
	public static class GunJson extends GunType< IGun< ? >, IGunPartRenderer< ? super IGun< ? > > >
	{
		@Override
		public IModular< ? > newPreparedContexted() { return this.new Gun<>(); }
		
		@Override
		public IModular< ? > deserializeContexted( NBTTagCompound nbt ) {
			return this.new Gun<>( nbt );
		}
		
		@Override
		protected GunWrapper< ?, ? > newWrapper( IModular< ? > primary, ItemStack stack ) {
			return new GunWrapper<>( primary, stack );
		}
	}
}
