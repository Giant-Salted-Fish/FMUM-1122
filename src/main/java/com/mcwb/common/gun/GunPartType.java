package com.mcwb.common.gun;

import com.google.gson.annotations.SerializedName;
import com.mcwb.client.gun.IGunPartRenderer;
import com.mcwb.client.render.IAnimator;
import com.mcwb.common.item.ModifiableItemType;
import com.mcwb.common.load.BuildableLoader;
import com.mcwb.common.meta.IMeta;
import com.mcwb.common.modify.IModifiable;
import com.mcwb.common.pack.IContentProvider;
import com.mcwb.util.ArmTracker;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public abstract class GunPartType< C extends IGunPart, M extends IGunPartRenderer< ? super C > >
	extends ModifiableItemType< C, M >
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
	
	protected class GunPart extends ModifiableItem implements IGunPart
	{
		protected short step;
		
		protected short offset;
		
		/**
		 * @see ModifiableItem#ModifiableItem()
		 */
		protected GunPart() { }
		
		/**
		 * @see ModifiableItem#ModifiableItem(NBTTagCompound)
		 */
		protected GunPart( NBTTagCompound nbt ) { super( nbt ); }
		
		@Override
		public int leftHandPriority() { return GunPartType.this.leftHandPriority; }
		
		@Override
		public int rightHandPriority() { return GunPartType.this.rightHandPriority; }
		
		@Override
		public int step() { return this.step; }
		
		@Override
		public void $step( int step )
		{
			this.step = ( short ) step;
			final int[] data = this.nbt.getIntArray( DATA_TAG );
			final int i = super.dataSize();
			data[ i ] = 0xFFFF0000 & data[ i ] | step;
			this.syncNBTData();
		}
		
		@Override
		public int offsetCount() { return GunPartType.this.offsets.length; }
		
		@Override
		public int offset() { return this.offset; }
		
		@Override
		public void $offset( int offset )
		{
			this.offset = ( short ) offset;
			final int[] data = this.nbt.getIntArray( DATA_TAG );
			final int i = super.dataSize();
			data[ i ] = 0xFFFF & data[ i ] | offset << 16;
			this.syncNBTData();
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
				.setupLeftArmToRender( leftArm, this.wrapperAnimator( animator ) );
		}
		
		@Override
		@SideOnly( Side.CLIENT )
		public void setupRightArmToRender( ArmTracker rightArm, IAnimator animator )
		{
			GunPartType.this.renderer
				.setupRightArmToRender( rightArm, this.wrapperAnimator( animator ) );
		}
		
		@Override
		protected int dataSize() { return super.dataSize() + 1; }
	}
	
	public static class GunPartJson
		extends GunPartType< IGunPart, IGunPartRenderer< ? super IGunPart > >
	{
		@Override
		public IModifiable newContexted( NBTTagCompound nbt ) { return this.new GunPart( nbt ); }
		
		@Override
		public IModifiable deserializeContexted( NBTTagCompound nbt )
		{
			final GunPart gunPart = this.new GunPart();
			gunPart.deserializeNBT( nbt );
			return gunPart;
		}
	}
}
