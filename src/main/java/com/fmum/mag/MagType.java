package com.fmum.mag;

import com.fmum.FMUM;
import com.fmum.ammo.IAmmoType;
import com.fmum.animation.SoundFrame;
import com.fmum.animation.Sounds;
import com.fmum.gunpart.GunPartType;
import com.fmum.item.IEquippedItem;
import com.fmum.item.IItem;
import com.fmum.item.IItemType;
import com.fmum.item.ItemCategory;
import com.fmum.load.IContentBuildContext;
import com.fmum.load.IContentLoader;
import com.fmum.load.IMeshLoadContext;
import com.fmum.load.JsonData;
import com.fmum.module.IModifyContext;
import com.fmum.module.IModule;
import com.fmum.module.IModuleType;
import com.fmum.paintjob.IPaintableType;
import com.fmum.render.IPreparedRenderer;
import com.fmum.render.ModelPath;
import gsf.util.animation.IAnimator;
import gsf.util.lang.Error;
import gsf.util.lang.Result;
import gsf.util.lang.Success;
import gsf.util.math.AxisAngle4f;
import gsf.util.math.Vec3f;
import gsf.util.render.GLUtil;
import gsf.util.render.Mesh;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumHand;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Optional;
import java.util.PrimitiveIterator;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import java.util.function.IntSupplier;
import java.util.function.Predicate;
import java.util.stream.IntStream;

public class MagType extends GunPartType
{
	public static final IContentLoader< MagType > LOADER = IContentLoader.of(
		MagType::new,
		IItemType.REGISTRY, IModuleType.REGISTRY, IPaintableType.REGISTRY
	);
	
	
	protected static final MagOpConfig
		OP_LOAD_AMMO = new MagOpConfig( 10, 7, new SoundFrame( 0.75F, Sounds.LOAD_AMMO ) );
	
	protected static final MagOpConfig
		OP_UNLOAD_AMMO = new MagOpConfig( 8, 6, new SoundFrame( 0.75F, Sounds.UNLOAD_AMMO ) );
	
	
	protected int capacity;
	
	protected Predicate< ItemCategory > ammo_predicate;
	
	protected MagOpConfig op_load_ammo;
	
	protected MagOpConfig op_unload_ammo;
	
	@SideOnly( Side.CLIENT )
	protected String loading_anim_channel;
	
	@SideOnly( Side.CLIENT )
	protected ModelPath follower_model_path;
	
	@SideOnly( Side.CLIENT )
	protected Vec3f[] follower_pos;
	
	@SideOnly( Side.CLIENT )
	protected AxisAngle4f[] follower_rot;
	
	@SideOnly( Side.CLIENT )
	protected Vec3f[] ammo_pos;
	
	@SideOnly(Side.CLIENT)
	protected AxisAngle4f[] ammo_rot;
	
	@SideOnly( Side.CLIENT )
	protected boolean is_dou_col_mag;
	
	@SideOnly( Side.CLIENT )
	protected Mesh follower_mesh;
	
	
	@Override
	public void reload( JsonData data, IContentBuildContext ctx )
	{
		super.reload( data, ctx );
		
		this.capacity = data.getInt( "capacity" ).orElse( 1 );
		this.ammo_predicate = data.getPredicate( "allowed_ammo", ItemCategory.class ).orElse( a -> false );
		this.op_load_ammo = data.get( "op_load_ammo", MagOpConfig.class ).orElse( OP_LOAD_AMMO );
		this.op_unload_ammo = data.get( "op_unload_ammo", MagOpConfig.class ).orElse( OP_UNLOAD_AMMO );
		FMUM.SIDE.runIfClient( () -> {
			this.loading_anim_channel = data.getString( "loading_anim_channel" ).orElse( "loading_mag" );
			this.follower_model_path = data.get( "follower_model", ModelPath.class ).orElse( ModelPath.NONE );
			this.follower_pos = data.get( "follower_pos", Vec3f[].class ).orElse( new Vec3f[ 0 ] );
			this.follower_rot = data.get( "follower_rot", AxisAngle4f[].class ).orElse( new AxisAngle4f[ 0 ] );
			this.ammo_pos = data.get( "ammo_pos", Vec3f[].class ).orElse( new Vec3f[ 0 ] );
			this.ammo_rot = data.get( "ammo_rot", AxisAngle4f[].class ).orElse( new AxisAngle4f[ 0 ] );
			this.is_dou_col_mag = data.getBool( "is_dou_col_mag" ).orElse( false );
		} );
	}
	
	@Override
	@SideOnly( Side.CLIENT )
	protected void _loadMesh( IMeshLoadContext ctx )
	{
		super._loadMesh( ctx );
		
		this.follower_mesh = ctx.loadMesh( this.follower_model_path ).orElse( Mesh.NONE );
	}
	
	@Override
	protected GunPart _createRawModule()
	{
		final Optional< Short > opt = IModuleType.REGISTRY.lookupID( this );
		return new Mag( opt.orElseThrow( IllegalStateException::new ) );
	}
	
	@Override
	public IModule takeAndDeserialize( NBTTagCompound nbt ) {
		return new Mag( nbt );
	}
	
	@Override
	protected IEquippedItem _newEquipped( EnumHand hand, IItem item, EntityPlayer player ) {
		return new EquippedMag();
	}
	
	
	protected static final String COUNT_AMMO_TAG = "ca";
	protected static final String LIST_AMMO_TAG = "la";
	protected class Mag extends GunPart implements IMag
	{
		protected String encoding_tag;
		
		/**
		 * Tracks the number of ammo in the magazine if {@link #encoding_tag} is
		 * {@link #COUNT_AMMO_TAG}, or the data array length need for count
		 * encoding if {@link #encoding_tag} is {@link #LIST_AMMO_TAG}.
		 */
		protected int cmp_ref_val;
		
		protected Mag( short id )
		{
			super( id );
			
			this.encoding_tag = COUNT_AMMO_TAG;
			this.cmp_ref_val = 0;
		}
		
		protected Mag( NBTTagCompound nbt )
		{
			super( nbt );
			
			if ( nbt.hasKey( COUNT_AMMO_TAG, NBT.TAG_INT_ARRAY ) )
			{
				this.encoding_tag = COUNT_AMMO_TAG;
				
				final int[] data = nbt.getIntArray( COUNT_AMMO_TAG );
				this.cmp_ref_val = ( int ) _Cnt$stream( data, false ).count();
			}
			else if ( nbt.hasKey( LIST_AMMO_TAG, NBT.TAG_INT_ARRAY ) )
			{
				this.encoding_tag = LIST_AMMO_TAG;
				
				final int[] data = nbt.getIntArray( LIST_AMMO_TAG );
				final PrimitiveIterator.OfInt itr = _Lst$stream( data, false ).iterator();
				int cnt_data_size = 0;
				int prev_id = 0;
				while ( itr.hasNext() )
				{
					final int id = itr.nextInt();
					cnt_data_size += id != prev_id ? 1 : 0;
					prev_id = id;
				}
				this.cmp_ref_val = cnt_data_size;
			}
			else
			{
				this.encoding_tag = COUNT_AMMO_TAG;
				this.cmp_ref_val = 0;
			}
		}
		
		@Override
		@SideOnly( Side.CLIENT )
		protected void _renderModel( IAnimator animator )
		{
			// Render ammo first as mag itself can be transparent.
			GL11.glPushMatrix();
			GLUtil.glMultMatrix( this.mat );
			
			final Vec3f[] ammo_pos = MagType.this.ammo_pos;
			final AxisAngle4f[] ammo_rot = MagType.this.ammo_rot;
			final int ammo_count = this.getAmmoCount();
			final boolean is_odd_cnt = ammo_count % 2 != 0;
			final boolean flip_pos_x = MagType.this.is_dou_col_mag && is_odd_cnt;
			final int[] data = this.nbt.getIntArray( this.encoding_tag );
			final IntStream stream = (
				this.encoding_tag.equals( COUNT_AMMO_TAG )
				? _Cnt$stream( data, true )
				: _Lst$stream( data, true )
			);
			final Iterator< IAmmoType > itr = (
				stream.mapToObj( i -> ( short ) i )
				.map( IAmmoType.REGISTRY::lookup )
				.map( opt -> opt.orElseThrow( IllegalArgumentException::new ) )
				.limit( ammo_pos.length )
				.iterator()
			);
			for ( int i = 0; itr.hasNext(); i += 1 )
			{
				GL11.glPushMatrix();
				final Vec3f pos = ammo_pos[ i ];
				final AxisAngle4f rot = i < ammo_rot.length ? ammo_rot[ i ] : AxisAngle4f.IDENTITY;
				GL11.glTranslatef( flip_pos_x ? -pos.x : pos.x, pos.y, pos.z );
				GLUtil.glRotateAA4f( rot );
				itr.next().renderModel( animator );
				GL11.glPopMatrix();
			}
			
			// Follower first for the same reason.
			final Vec3f[] flw_pos = MagType.this.follower_pos;
			if ( flw_pos.length > 0 )
			{
				final AxisAngle4f[] flw_rot = MagType.this.follower_rot;
				final int idx = Math.min( ammo_count, flw_pos.length - 1 );
				final Vec3f pos = flw_pos[ idx ];
				final AxisAngle4f rot = idx < flw_rot.length ? flw_rot[ idx ] : AxisAngle4f.IDENTITY;
				GLUtil.glTranslateV3f( pos );
				GLUtil.glRotateAA4f( rot );
				GLUtil.glScale1f( MagType.this.fp_scale );
				GLUtil.bindTexture( this._getTexture() );
				MagType.this.follower_mesh.draw();
			}
			GL11.glPopMatrix();
			
			super._renderModel( animator );
		}
		
		@Override
		@SideOnly( Side.CLIENT )
		public IModule IGunPart$createSelectionProxy( IModifyContext ctx )
		{
			// TODO: Impl.
			return super.IGunPart$createSelectionProxy( ctx );
		}
		
		@Override
		@SideOnly( Side.CLIENT )
		public IMag IMag$createLoadingProxy()
		{
			return new Mag( Mag.this.nbt.copy() ) {
				@Override
				public void IGunPart$prepareRender(
					int base_slot_idx,
					IAnimator animator,
					Consumer< IPreparedRenderer > render_queue
				) {
					final IAnimator wrapper = channel -> animator.getChannel(
						channel.equals( MagType.this.mod_anim_channel )
						? MagType.this.loading_anim_channel
						: channel
					);
					super.IGunPart$prepareRender( base_slot_idx, wrapper, render_queue );
				}
			};
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
				? _Lst$getCount( data )
				: this.cmp_ref_val
			);
		}
		
		@Override
		public Result< IntSupplier, String > checkAmmoForLoad( IAmmoType ammo )
		{
			return (
				MagType.this.ammo_predicate.test( ammo.getCategory() )
				? new Success<>( () -> this._loadAmmo( ammo ) )
				: new Error<>( "incompatible_ammo_type" )
			);
		}
		
		@Override
		public Optional< ? extends IAmmoType > peekAmmo()
		{
			final int[] data = this.nbt.getIntArray( this.encoding_tag );
			final int value = data[ data.length - 1 ];
			final short ammo_id = ( short ) (
				this.encoding_tag.equals( LIST_AMMO_TAG )
				? _Lst$peekAmmo( value )
				: ( value >>> 16 )
			);
			return IAmmoType.REGISTRY.lookup( ammo_id );
		}
		
		protected int _loadAmmo( IAmmoType ammo )
		{
			final int ammo_id = 0xFFFF & (
				IAmmoType.REGISTRY.lookupID( ammo )
				.orElseThrow( IllegalArgumentException::new )
			);
			
			final int[] data = this.nbt.getIntArray( this.encoding_tag );
			if ( this.encoding_tag.equals( LIST_AMMO_TAG ) )
			{
				final int ammo_count = _Lst$getCount( data ) + 1;
				final int lst_data_size = ( 1 + ammo_count ) / 2;
				
				final boolean is_diff_ammo;
				if ( data.length > 0 )
				{
					final int value = data[ data.length - 1 ];
					final int last_id = _Lst$peekAmmo( value );
					is_diff_ammo = ammo_id != last_id;
				}
				else {
					is_diff_ammo = true;
				}
				
				if ( is_diff_ammo ) {
					this.cmp_ref_val += 1;
				}
				else if ( this.cmp_ref_val < lst_data_size )
				{
					final int cnt_data_size = this.cmp_ref_val;
					this.cmp_ref_val = ammo_count;
					
					final int[] new_data = _Cnt$fromLst( cnt_data_size, data );
					new_data[ cnt_data_size - 1 ] += 1;  // Add new ammo.
					this.nbt.setIntArray( COUNT_AMMO_TAG, new_data );
					this.encoding_tag = COUNT_AMMO_TAG;
					this.nbt.removeTag( LIST_AMMO_TAG );
					return ammo_count;  // Careful! This is a return.
				}
				
				if ( lst_data_size > data.length )
				{
					final int[] new_data = new int[ lst_data_size ];
					System.arraycopy( data, 0, new_data, 0, data.length );
					new_data[ lst_data_size - 1 ] = ammo_id;
					this.nbt.setIntArray( LIST_AMMO_TAG, new_data );
				}
				else {
					data[ lst_data_size - 1 ] |= ammo_id << 16;
				}
				return ammo_count;
			}
			else  // COUNT AMMO
			{
				final int ammo_count = 1 + this.cmp_ref_val;
				final boolean is_same_ammo;
				if ( data.length > 0 )
				{
					final int value = data[ data.length - 1 ];
					final int last_id = value >>> 16;
					is_same_ammo = ammo_id == last_id;
				}
				else {
					is_same_ammo = false;
				}
				
				if ( is_same_ammo )
				{
					this.cmp_ref_val = ammo_count;
					data[ data.length - 1 ] += 1;
					return ammo_count;  // Careful! This is a return.
				}
				
				final int lst_data_size = ( 1 + ammo_count ) / 2;
				final int cnt_data_size = data.length + 1;
				if ( lst_data_size < cnt_data_size )
				{
					this.cmp_ref_val = cnt_data_size;
					
					final int[] new_data = _Lst$fromCnt( lst_data_size, data );
					new_data[ lst_data_size - 1 ] |= ammo_id << 16;
					this.nbt.setIntArray( LIST_AMMO_TAG, new_data );
					this.encoding_tag = LIST_AMMO_TAG;
					this.nbt.removeTag( COUNT_AMMO_TAG );
				}
				else
				{
					this.cmp_ref_val = ammo_count;
					final int[] new_data = new int[ cnt_data_size ];
					System.arraycopy( data, 0, new_data, 0, data.length );
					new_data[ cnt_data_size - 1 ] = ammo_id << 16;
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
//				this.encoding_tag = COUNT_AMMO_TAG;  // Not necessary.
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
				final PrimitiveIterator.OfInt itr = _Lst$stream( data, true ).iterator();
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
						final int[] new_data = _Cnt$fromLst( cnt_data_size, data );
						this.nbt.setIntArray( COUNT_AMMO_TAG, new_data );
						this.encoding_tag = COUNT_AMMO_TAG;
						this.nbt.removeTag( LIST_AMMO_TAG );
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
					this.cmp_ref_val -= 1;
					final int[] new_data = new int[ data.length - 1 ];
					System.arraycopy( data, 0, new_data, 0, new_data.length );
					this.nbt.setIntArray( COUNT_AMMO_TAG, new_data );
					return lookup.apply( popped_id );
				}
				
				final int lst_data_size = this.cmp_ref_val / 2;
				if ( lst_data_size < data.length )
				{
					this.cmp_ref_val = data.length;
					data[ data.length - 1 ] -= 1;
					final int[] new_data = _Lst$fromCnt( lst_data_size, data );
					this.nbt.setIntArray( LIST_AMMO_TAG, new_data );
					this.encoding_tag = LIST_AMMO_TAG;
					this.nbt.removeTag( COUNT_AMMO_TAG );
				}
				else
				{
					this.cmp_ref_val -= 1;
					data[ data.length - 1 ] -= 1;
				}
				return lookup.apply( popped_id );
			}
		}
	}
	
	protected static int _Lst$getCount( int[] data )
	{
		final int len = data.length;
		final boolean is_odd_count = len > 0 && data[ len - 1 ] >>> 16 == 0;
		return 2 * data.length - ( is_odd_count ? 1 : 0 );
	}
	
	/**
	 * First in first out when {@code reverse} is {@code false}, otherwise
	 * last in first out.
	 */
	protected static IntStream _Lst$stream( int[] data, boolean reverse )
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
	
	protected static void _Lst$setAmmo( int[] data, int idx, int ammo_id )
	{
		final int i = idx / 2;
		final int offset = idx % 2 != 0 ? 16 : 0;
		final int masked = data[ i ] & ~( 0xFFFF << offset );
		data[ i ] = masked | ( ammo_id << offset );
	}
	
	protected static int _Lst$peekAmmo( int value )
	{
		final int first = 0xFFFF & value;
		final int second = value >>> 16;
		return second == 0 ? first : second;
	}
	
	/**
	 * First in first out when {@code reverse} is {@code false}, otherwise
	 * last in first out.
	 */
	protected static int[] _Lst$fromCnt( int len, int[] src )
	{
		final int[] data = new int[ len ];
		final PrimitiveIterator.OfInt itr = _Cnt$stream( src, false ).iterator();
		for ( int i = 0; itr.hasNext(); i += 1 ) {
			_Lst$setAmmo( data, i, itr.nextInt() );
		}
		return data;
	}
	
	protected static IntStream _Cnt$stream( int[] data, boolean reverse )
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
	
	protected static int[] _Cnt$fromLst( int len, int[] src )
	{
		final int[] data = new int[ len ];
		final PrimitiveIterator.OfInt itr = _Lst$stream( src, false ).iterator();
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
