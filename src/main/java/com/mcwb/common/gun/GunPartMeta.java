package com.mcwb.common.gun;

import javax.annotation.Nullable;

import com.google.gson.annotations.SerializedName;
import com.mcwb.client.gun.IGunPartRenderer;
import com.mcwb.common.item.ModifiableItemMeta;
import com.mcwb.common.load.BuildableLoader;
import com.mcwb.common.meta.IMeta;
import com.mcwb.common.modify.IContextedModifiable;
import com.mcwb.common.pack.IContentProvider;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;

public abstract class GunPartMeta<
	C extends IContextedGunPart,
	M extends IGunPartRenderer< ? super C >
> extends ModifiableItemMeta< C, M >
{
	public static final BuildableLoader< IMeta >
		LOADER = new BuildableLoader<>( "gun_part", GunPartJson.class );
	
	protected static final float[] OFFSETS = { 0F };
	
	@SerializedName( value = "offsets", alternate = "installOffsets" )
	protected float[] offsets = OFFSETS;
	
	@Override
	public IMeta build( String name, IContentProvider provider )
	{
		super.build( name, provider );
		
		// TODO: scale aim center
		for( int i = this.offsets.length; i-- > 0; this.offsets[ i ] *= this.paramScale );
		
//		FIXME: if( MCWB.MOD.isClient() && MetaModular.REGISTRY.get( this.indicator ) == null )
//		this.indicator = MCWBClient.MODULE_INDICATOR;
		return this;
	}
	
	@Override
	protected IMeta loader() { return LOADER; }
	
	protected abstract class ContextedGunPart extends ContextedModifiableItem
		implements IContextedGunPart
	{
		protected short step;
		
		protected short offset;
		
		/**
		 * @see ContextedModifiableItem#ContextedModifiableItem()
		 */
		protected ContextedGunPart() { }
		
		/**
		 * @see ContextedModifiableItem#ContextedModifiableItem(NBTTagCompound)
		 */
		protected ContextedGunPart( NBTTagCompound nbt ) { super( nbt ); }
		
		@Override
		public boolean hasCapability( Capability< ? > capability, @Nullable EnumFacing facing )
		{
			return capability == IContextedGunPart.CAPABILITY
				|| capability == IContextedModifiable.CAPABILITY;
		}
		
		@Override
		public int step() { return this.step; }
		
		@Override
		public void $step( int step )
		{
			this.step = ( short ) step;
			final int[] data = nbt.getIntArray( DATA_TAG );
			final int i = super.dataSize();
			data[ i ] = 0xFFFF0000 & data[ i ] | this.step;
		}
		
		@Override
		public int offsetCount() { return GunPartMeta.this.offsets.length; }
		
		@Override
		public int offset() { return this.offset; }
		
		@Override
		public void $offset( int offset )
		{
			this.offset = ( short ) offset;
			final int[] data = this.nbt.getIntArray( DATA_TAG );
			final int i = super.dataSize();
			data[ i ] = 0xFFFF & data[ i ] | offset << 16;
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
		protected int dataSize() { return super.dataSize() + 1; }
	}
	
	public static class GunPartJson extends GunPartMeta<
		IContextedGunPart,
		IGunPartRenderer< ? super IContextedGunPart >
	> {
		@Override
		protected Capability< ? extends IContextedGunPart > capability() {
			return IContextedGunPart.CAPABILITY;
		}
		
		@Override
		protected ContextedGunPart newCtxedCap( NBTTagCompound nbt )
		{
			return this.new ContextedGunPart( nbt )
			{
				@Override
				protected IContextedGunPart self() { return this; }
			};
		}
		
		@Override
		protected ContextedGunPart newRawCtxedCap()
		{
			return new ContextedGunPart()
			{
				@Override
				protected IContextedGunPart self() { return this; }
			};
		}
	}
}
