package com.fmum.common.gun;

import com.fmum.common.item.ItemType;
import com.fmum.common.load.IContentBuildContext;
import com.fmum.common.module.IModuleSlot;
import com.fmum.common.module.IModuleType;
import com.fmum.common.module.Module;
import com.fmum.common.paintjob.IPaintableType;
import com.fmum.common.paintjob.IPaintjob;
import com.fmum.util.Category;
import com.fmum.util.Mesh;
import com.google.gson.annotations.SerializedName;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class GunPartType extends ItemType implements IModuleType, IPaintableType
{
	private static final float[] DEFAULT_OFFSETS = { 0.0F };
	
	
	protected Category category;
	
	protected float param_scale = 1.0F;
	
	protected List< IModuleSlot > slots = Collections.emptyList();
	
	protected float[] offsets = DEFAULT_OFFSETS;
	
	protected List< IPaintjob > paintjobs = Collections.emptyList();
	
	@SideOnly( Side.CLIENT )
	@SerializedName( value = "mesh", alternate = "model" )
	protected MeshInfo mesh_info;
	
	@SideOnly( Side.CLIENT )
	protected transient Mesh mesh;
	
	@Override
	public void buildServerSide( IContentBuildContext ctx )
	{
		super.buildServerSide( ctx );
		
		IModuleType.REGISTRY.regis( this );
		IPaintableType.REGISTRY.regis( this );
		
		// Set it as not stackable.
		this.setMaxStackSize( 1 );
		
		// Check member variable setup.
		this.category = Optional.ofNullable( this.category )
			.orElseGet( () -> new Category( this.name ) );
		// TODO: Modify indicator.
		
		// Regis itself as the default paintjob.
		if ( this.paintjobs.isEmpty() ) {
			this.paintjobs = new ArrayList<>();
		}
		this.paintjobs.add( 0, () -> this.texture );
		
		// Apply param scale.
		this.slots.forEach( slot -> slot.scaleParam( this.param_scale ) );
		// TODO: Scale hit boxes.
	}
	
	@Override
	public void buildClientSide( IContentBuildContext ctx )
	{
		super.buildClientSide( ctx );
		
		
	}
	
	@Override
	public void injectPaintjob( IPaintjob paintjob ) {
		this.paintjobs.add( paintjob );
	}
	
	/**
	 * <p> {@inheritDoc} </p>
	 *
	 * <p> In default avoid to break the block when holding a this item in
	 * survive mode. </p>
	 */
	@Override
	public boolean onBlockStartBreak(
		@Nonnull ItemStack itemstack,
		@Nonnull BlockPos pos,
		@Nonnull EntityPlayer player
	) { return true; }
	
	/**
	 * <p> {@inheritDoc} </p>
	 *
	 * <p> In default avoid to break the block when holding this item in creative mode. </p>
	 */
	@Override
	public boolean canDestroyBlockInCreative(
		@Nonnull World world,
		@Nonnull BlockPos pos,
		@Nonnull ItemStack stack,
		@Nonnull EntityPlayer player
	) { return false; }
	
	protected class GunPart< I extends IGunPart< ? extends I > >
		extends Module< I > implements IGunPart< I >
	{
		protected short offset;
		protected short step;
		
		protected GunPart() { }
		
		protected GunPart( NBTTagCompound nbt )
		{
			super( nbt );
			
			// FIXME
		}
		
		@Override
		public int stackId() {
			throw new RuntimeException();
		}
		
		@Override
		public String name() {
			return GunPartType.this.name;
		}
		
		@Override
		public Category category() {
			return GunPartType.this.category;
		}
		
		@Override
		public int paintjobCount() {
			return GunPartType.this.paintjobs.size();
		}
		
		@Override
		public int slotCount() {
			return GunPartType.this.slots.size();
		}
		
		@Override
		public IModuleSlot getSlot( int idx ) {
			return GunPartType.this.slots.get( idx );
		}
		
		@Override
		public int offsetCount() {
			return GunPartType.this.offsets.length;
		}
		
		@Override
		public int offset() {
			return this.offset;
		}
		
		@Override
		public int step() {
			return this.step;
		}
		
		@Override
		public void deserializeNBT( NBTTagCompound nbt )
		{
			super.deserializeNBT( nbt );
			
			final int[] data = nbt.getIntArray( DATA_TAG );
			final int value = data[ super._dataSize() ];
			this.offset = ( short ) value;
			this.step = ( short ) ( value >>> 16 );
		}
		
		@Override
		public String toString() {
			return String.format( "Item<%s>", GunPartType.this );
		}
		
		@Override
		protected int _id() {
			return IModuleType.REGISTRY.getID( GunPartType.this );
		}
		
		@Override
		protected int _dataSize() {
			return 1 + super._dataSize();
		}
		
		
	}
}
