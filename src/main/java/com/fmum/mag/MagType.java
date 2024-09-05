package com.fmum.mag;

import com.fmum.ammo.IAmmoType;
import com.fmum.gunpart.GunPartType;
import com.fmum.item.ItemCategory;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import net.minecraft.nbt.NBTTagCompound;

import java.util.Arrays;
import java.util.Optional;
import java.util.PrimitiveIterator.OfInt;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.stream.IntStream;

public class MagType extends GunPartType
{
	@Expose
	protected int capacity = 1;
	
	@Expose
	@SerializedName( "allowed_ammo" )
	Predicate< ItemCategory > ammo_predicate;
	
	
	protected static final String LIST_AMMO_TAG = "la";
	protected static final String COUNT_AMMO_TAG = "ca";
	
	protected class Mag extends GunPart implements IMag
	{
		protected String encoding_tag;
		
		/**
		 * Tracks the number of ammo in the magazine if {@link #encoding_tag} is
		 * {@link #COUNT_AMMO_TAG}, or the data array length need for count
		 * encoding if {@link #encoding_tag} is {@link #LIST_AMMO_TAG}.
		 */
		protected int cmp_ref_val;
		
		protected Mag()
		{
			this.encoding_tag = LIST_AMMO_TAG;
			this.cmp_ref_val = 0;
		}
		
		protected Mag( NBTTagCompound nbt ) {
			super( nbt );
		}
		
		@Override
		public int getCapacity() {
			return MagType.this.capacity;
		}
		
		@Override
		public int getAmmoCount()
		{
			final int[] data = this.nbt.getIntArray( this.encoding_tag );
			return(
				this.encoding_tag.equals( LIST_AMMO_TAG )
				? this._Lst$getCount( data )
				: this.cmp_ref_val
			);
		}
		
		@Override
		public boolean canLoadAmmo( IAmmoType ammo ) {
			return MagType.this.ammo_predicate.test( ammo.getCategory() );
		}
		
		@Override
		public Optional< ? extends IAmmoType > peekAmmo()
		{
			final int[] data = this.nbt.getIntArray( this.encoding_tag );
			final int value = data[ data.length - 1 ];
			final short ammo_id = ( short ) (
				this.encoding_tag.equals( LIST_AMMO_TAG )
				? Math.max( 0xFFFF & value, value >>> 16 )
				: ( value >>> 16 )
			);
			return IAmmoType.REGISTRY.lookup( ammo_id );
		}
		
		@Override
		public int loadAmmo( IAmmoType ammo )
		{
			final int ammo_id = 0xFFFF & (
				IAmmoType.REGISTRY.lookupID( ammo )
				.orElseThrow( IllegalArgumentException::new )
			);
			
			final int[] data = this.nbt.getIntArray( this.encoding_tag );
			final int value = data[ data.length - 1 ];
			if ( this.encoding_tag.equals( LIST_AMMO_TAG ) )
			{
				final int ammo_count = this._Lst$getCount( data ) + 1;
				final int lst_data_size = ( 1 + ammo_count ) / 2;
				
				final int last_id = Math.max( 0xFFFF & value, value >>> 16 );
				final boolean is_diff_ammo = ammo_id != last_id;
				if ( is_diff_ammo ) {
					this.cmp_ref_val += 1;
				}
				else if ( this.cmp_ref_val < lst_data_size )
				{
					final int cnt_data_size = this.cmp_ref_val;
					this.cmp_ref_val = ammo_count;
					
					final int[] new_data = this._Lst$fromCnt( cnt_data_size, data );
					data[ cnt_data_size - 1 ] += 1;  // Add new ammo.
					this.nbt.setIntArray( COUNT_AMMO_TAG, new_data );
					return ammo_count;  // Careful! This is a return.
				}
				
				if ( lst_data_size > data.length )
				{
					final int[] new_data = new int[ lst_data_size ];
					System.arraycopy( data, 0, new_data, 0, data.length );
					data[ lst_data_size - 1 ] = ammo_id;
					this.nbt.setIntArray( LIST_AMMO_TAG, new_data );
				}
				else {
					data[ lst_data_size - 1 ] |= ammo_id << 16;
				}
				return ammo_count;
			}
			else  // COUNT AMMO
			{
				final int last_id = value >>> 16;
				final int ammo_count = 1 + this.cmp_ref_val;
				final boolean is_same_ammo = ammo_id == last_id;
				if ( is_same_ammo )
				{
					this.cmp_ref_val = ammo_count;
					data[ data.length - 1 ] += 1;
					return ammo_count;
				}
				
				final int lst_data_size = ( 1 + this.cmp_ref_val ) / 2;
				final int cnt_data_size = data.length + 1;
				if ( lst_data_size < cnt_data_size )
				{
					this.cmp_ref_val = cnt_data_size;
					
					final int[] new_data = this._Lst$fromCnt( lst_data_size, data );
					new_data[ lst_data_size - 1 ] |= ammo_id << 16;
					this.nbt.setIntArray( LIST_AMMO_TAG, new_data );
				}
				else
				{
					this.cmp_ref_val = ammo_count;
					final int[] new_data = new int[ cnt_data_size ];
					System.arraycopy( data, 0, new_data, 0, data.length );
					data[ cnt_data_size - 1 ] = ammo_id << 16;
					this.nbt.setIntArray( COUNT_AMMO_TAG, new_data );
				}
				return ammo_count;
			}
		}
		
		@Override
		public IAmmoType popAmmo()
		{
			final int[] data = this.nbt.getIntArray( this.encoding_tag );
			final int ammo_count = this.getAmmoCount();
			if ( ammo_count == 1 )
			{
				final Optional< ? extends IAmmoType > ammo = this.peekAmmo();
				this.nbt.removeTag( this.encoding_tag );
				this.cmp_ref_val = 0;
				return ammo.orElseThrow( IllegalStateException::new );
			}
			
			final IntFunction< IAmmoType > lookup = ammo_id -> (
				IAmmoType.REGISTRY.lookup( ( short ) ammo_id )
				.orElseThrow( IllegalStateException::new )
			);
			if ( this.encoding_tag.equals( LIST_AMMO_TAG ) )
			{
				final int lst_data_size = ammo_count / 2;
				final OfInt itr = this._Lst$stream( data, true ).iterator();
				final int popped_id = itr.nextInt();
				final int last_id = itr.nextInt();
				final boolean is_diff_ammo = popped_id != last_id;
				if ( is_diff_ammo )
				{
					final int cnt_data_size = this.cmp_ref_val - 1;
					if ( cnt_data_size < lst_data_size )
					{
						this.cmp_ref_val = ammo_count - 1;
						data[ lst_data_size - 1 ] &= 0xFFFF;
						final int[] new_data = this._Cnt$fromLst( cnt_data_size, data );
						this.nbt.setIntArray( COUNT_AMMO_TAG, new_data );
						return lookup.apply( popped_id );
					}
					else {
						this.cmp_ref_val = cnt_data_size;
					}
				}
				
				if ( lst_data_size < data.length )
				{
					final int[] new_data = new int[ lst_data_size ];
					System.arraycopy( data, 0, new_data, 0, lst_data_size );
					this.nbt.setIntArray( LIST_AMMO_TAG, new_data );
				}
				else {
					data[ lst_data_size - 1 ] &= 0xFFFF;
				}
				return lookup.apply( popped_id );
			}
			else  // COUNT AMMO.
			{
				final int value = data[ data.length - 1 ];
				final int popped_id = value >>> 16;
				final int last_cnt = 0xFFFF & value;
				final boolean is_diff_ammo = last_cnt == 0;
				if ( is_diff_ammo )
				{
					final int[] new_data = new int[ data.length - 1 ];
					System.arraycopy( data, 0, new_data, 0, new_data.length );
					this.nbt.setIntArray( COUNT_AMMO_TAG, new_data );
					return lookup.apply( popped_id );
				}
				
				data[ data.length - 1 ] -= 1;
				final int lst_data_size = this.cmp_ref_val / 2;
				if ( lst_data_size < data.length )
				{
					this.cmp_ref_val = data.length;
					final int[] new_data = this._Lst$fromCnt( lst_data_size, data );
					this.nbt.setIntArray( LIST_AMMO_TAG, new_data );
				}
				else {
					this.cmp_ref_val -= 1;
				}
				return lookup.apply( popped_id );
			}
		}
		
		protected final int _Lst$getCount( int[] data )
		{
			final int len = data.length;
			final boolean is_odd_count = len > 0 && data[ len - 1 ] >>> 16 == 0;
			return 2 * data.length - ( is_odd_count ? 1 : 0 );
		}
		
		protected final IntStream _Lst$stream( int[] data, boolean reverse )
		{
			final int count = _Lst$getCount( data );
			final IntStream raw_idx = IntStream.range( 0, count );
			final IntStream index_stream = reverse ? raw_idx.map( i -> count - i - 1 ) : raw_idx;
			return index_stream.map( i -> {
				final int idx = i / 2;
				final int offset = i % 2 == 0 ? 0 : 16;
				return 0xFFFF & ( data[ idx ] >>> offset );
			} );
		}
		
		protected final void _Lst$setAmmo( int[] data, int idx, int ammo_id )
		{
			final int i = idx / 2;
			final int offset = idx % 2 != 0 ? 16 : 0;
			final int masked = data[ i ] & ~( 0xFFFF << offset );
			data[ i ] = masked | ( ammo_id << offset );
		}
		
		protected final int[] _Lst$fromCnt( int len, int[] src )
		{
			final int[] data = new int[ len ];
			final OfInt itr = this._Cnt$stream( src, false ).iterator();
			for ( int i = 0; itr.hasNext(); i += 1 ) {
				this._Lst$setAmmo( data, i, itr.nextInt() );
			}
			return data;
		}
		
		protected final IntStream _Cnt$stream( int[] data, boolean reverse )
		{
			final IntStream value_stream = (
				reverse
				? IntStream.range( 0, data.length ).map( i -> data[ data.length - i - 1 ] )
				: Arrays.stream( data )
			);
			return value_stream.flatMap( value -> {
				final int ammo_id = value >>> 16;
				final int count = 1 + ( 0xFFFF & value );
				return IntStream.generate( () -> ammo_id ).limit( count );
			} );
		}
		
		protected final int[] _Cnt$fromLst( int len, int[] src )
		{
			final int[] data = new int[ len ];
			final OfInt itr = this._Lst$stream( src, false ).iterator();
			int prev_id = 0;
			for ( int i = -1; itr.hasNext(); )
			{
				final int id = itr.nextInt();
				if ( id != prev_id )
				{
					i += 1;
					data[ i ] = id << 16;
					prev_id = id;
				}
				else {
					data[ i ] += 1;
				}
			}
			return data;
		}
	}
}
