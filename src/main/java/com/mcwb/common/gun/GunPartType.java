package com.mcwb.common.gun;

import com.google.gson.annotations.SerializedName;
import com.mcwb.client.gun.IGunPartRenderer;
import com.mcwb.client.render.IAnimator;
import com.mcwb.common.item.ModifiableItemType;
import com.mcwb.common.load.BuildableLoader;
import com.mcwb.common.load.IContentProvider;
import com.mcwb.common.meta.IMeta;
import com.mcwb.common.module.IModular;
import com.mcwb.util.ArmTracker;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public abstract class GunPartType<
	C extends IGunPart< ? >,
	R extends IGunPartRenderer< ? super C >
> extends ModifiableItemType< C, R >
{
	public static final BuildableLoader< IMeta >
		LOADER = new BuildableLoader<>( "gun_part", GunPartJson.class );
	
	protected static final float[] OFFSETS = { 0F };
	
	@SerializedName( value = "offsets", alternate = "installOffsets" )
	protected float[] offsets = OFFSETS;
	
	protected int leftHandPriority = Integer.MIN_VALUE; // TODO: move to grip type maybe?
	protected int rightHandPriority = Integer.MIN_VALUE;
	
	@Override
	public IMeta build( String name, IContentProvider provider )
	{
		super.build( name, provider );
		
		// TODO: scale aim center
		for( int i = this.offsets.length; i-- > 0; this.offsets[ i ] *= this.paramScale );
		return this;
	}
	
	@Override
	protected IMeta loader() { return LOADER; }
	
	protected class GunPart< T extends IGunPart< ? extends T > >
		extends ModifiableItem< T > implements IGunPart< T >
	{
		protected short offset;
		protected short step;
		
		protected GunPart() { }
		
		protected GunPart( NBTTagCompound nbt ) { super( nbt ); }
		
		@Override
		public int offsetCount() { return GunPartType.this.offsets.length; }
		
		@Override
		public int offset() { return this.offset; }
		
		@Override
		public int step() { return this.step; }
		
		@Override
		public void setOffsetStep( int offset, int step )
		{
			this.offset = ( short ) offset;
			this.step = ( short ) step;
			final int[] data = this.nbt.getIntArray( DATA_TAG );
			data[ super.dataSize() ] = 0xFFFF & offset | step << 16;
			this.syncAndUpdate();
		}
		
		@Override
		public int leftHandPriority() { return GunPartType.this.leftHandPriority; }
		
		@Override
		public int rightHandPriority() { return GunPartType.this.rightHandPriority; }
		
		@Override
		@SideOnly( Side.CLIENT )
		public void setupLeftArmToRender( ArmTracker leftArm, IAnimator animator )
		{
			final IAnimator wrappedAnimator = this.wrapAnimator( animator );
			GunPartType.this.renderer.setupLeftArmToRender( leftArm, wrappedAnimator );
		}
		
		@Override
		@SideOnly( Side.CLIENT )
		public void setupRightArmToRender( ArmTracker rightArm, IAnimator animator )
		{
			final IAnimator wrappedAnimator = this.wrapAnimator( animator );
			GunPartType.this.renderer.setupRightArmToRender( rightArm, wrappedAnimator );
		}
		
		@Override
		public void deserializeNBT( NBTTagCompound nbt )
		{
			super.deserializeNBT( nbt );
			
			final int[] data = nbt.getIntArray( DATA_TAG );
			final int value = data[ super.dataSize() ];
			this.offset = ( short ) value;
			this.step = ( short ) ( value >>> 16 );
		}
		
		@Override
		protected int dataSize() { return super.dataSize() + 1; }
	}
	
	protected static class GunPartWrapper<
		M extends IGunPart< ? extends M >,
		T extends IGunPart< ? extends M >
	> extends ModifiableItemWrapper< M, T > implements IGunPart< M >
	{
		protected GunPartWrapper( T primary, ItemStack stack ) { super( primary, stack ); }
		
		@Override
		public int leftHandPriority() { return this.primary.leftHandPriority(); }
		
		@Override
		public int rightHandPriority() { return this.primary.rightHandPriority(); }
		
		@Override
		@SideOnly( Side.CLIENT )
		public void setupLeftArmToRender( ArmTracker leftArm, IAnimator animator ) {
			throw new RuntimeException();
		}
		
		@Override
		@SideOnly( Side.CLIENT )
		public void setupRightArmToRender( ArmTracker rightArm, IAnimator animator ) {
			throw new RuntimeException();
		}
	}
	
	private static class GunPartJson
		extends GunPartType< IGunPart< ? >, IGunPartRenderer< ? super IGunPart< ? > > >
	{
		@Override
		public IModular< ? > newRawContexted()
		{
			return this.new GunPart< IGunPart< ? > >()
			{
				// Override this so that we do not need to create a wrapper for it
				@Override
				public void syncAndUpdate() { }
			};
		}
		
		@Override
		public IModular< ? > deserializeContexted( NBTTagCompound nbt ) {
			return this.new GunPart<>( nbt );
		}
		
		@Override
		protected ICapabilityProvider newWrapper( IGunPart< ? > primary, ItemStack stack ) {
			return new GunPartWrapper<>( primary, stack );
		}
	}
}
