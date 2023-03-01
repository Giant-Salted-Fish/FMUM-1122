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
		protected short step;
		
		protected short offset;
		
		protected GunPart() { }
		
		protected GunPart( NBTTagCompound nbt ) { super( nbt ); }
		
		@Override
		public int leftHandPriority() { return GunPartType.this.leftHandPriority; }
		
		@Override
		public int rightHandPriority() { return GunPartType.this.rightHandPriority; }
		
		@Override
		public int offsetCount() { return GunPartType.this.offsets.length; }
		
		@Override
		public int offset() { return this.offset; }
		
		@Override
		public int step() { return this.step; }
		
		@Override
		public void updateOffsetStep( int offset, int step )
		{
			this.offset = ( short ) offset;
			this.step = ( short ) step;
			final int[] data = this.nbt.getIntArray( DATA_TAG );
			data[ super.dataSize() ] = 0xFFFF & step | offset << 16;
			this.updateState(); // Because position has changed
		}
		
		@Override
		public void deserializeNBT( NBTTagCompound nbt )
		{
			super.deserializeNBT( nbt );
			
			final int[] data = nbt.getIntArray( DATA_TAG );
			final int value = data[ super.dataSize() ];
			this.step = ( short ) value;
			this.offset = ( short ) ( value >>> 16 );
		}
		
		@Override
		@SideOnly( Side.CLIENT )
		public void setupLeftArmToRender( ArmTracker leftArm, IAnimator animator )
		{
			GunPartType.this.renderer
				.setupLeftArmToRender( leftArm, this.wrapAnimator( animator ) );
		}
		
		@Override
		@SideOnly( Side.CLIENT )
		public void setupRightArmToRender( ArmTracker rightArm, IAnimator animator )
		{
			GunPartType.this.renderer
				.setupRightArmToRender( rightArm, this.wrapAnimator( animator ) );
		}
		
		@Override
		protected int dataSize() { return super.dataSize() + 1; }
	}
	
	protected static class GunPartWrapper<
		M extends IGunPart< ? extends M >,
		T extends IGunPart< ? extends M >
	> extends ModifiableItemWrapper< M, T > implements IGunPart< M >
	{
		protected GunPartWrapper( IModular< ? > primary, ItemStack stack ) {
			super( primary, stack );
		}
		
		@Override
		public int leftHandPriority() { return this.primary.leftHandPriority(); }
		
		@Override
		public int rightHandPriority() { return this.primary.rightHandPriority(); }
		
		@Override
		@SideOnly( Side.CLIENT )
		public void setupLeftArmToRender( ArmTracker leftArm, IAnimator animator ) {
			this.primary.setupLeftArmToRender( leftArm, animator );
		}
		
		@Override
		@SideOnly( Side.CLIENT )
		public void setupRightArmToRender( ArmTracker rightArm, IAnimator animator ) {
			this.primary.setupRightArmToRender( rightArm, animator );
		}
	}
	
	public static class GunPartJson
		extends GunPartType< IGunPart< ? >, IGunPartRenderer< ? super IGunPart< ? > > >
	{
		@Override
		public IModular< ? > newPreparedContexted() { return this.new GunPart<>(); }
		
		@Override
		public IModular< ? > deserializeContexted( NBTTagCompound nbt ) {
			return this.new GunPart<>( nbt );
		}
		
		@Override
		protected GunPartWrapper< ?, ? > newWrapper( IModular< ? > primary, ItemStack stack ) {
			return new GunPartWrapper<>( primary, stack );
		}
	}
}
