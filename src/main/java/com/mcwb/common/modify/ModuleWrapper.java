package com.mcwb.common.modify;

import java.util.Collection;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import com.google.common.base.Supplier;
import com.mcwb.client.IAutowireSmoother;
import com.mcwb.client.item.ItemAnimatorState;
import com.mcwb.client.modify.IMultPassRenderer;
import com.mcwb.client.render.IAnimator;
import com.mcwb.util.Mat4f;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Also acts like a {@link ICapabilityProvider}
 * 
 * @author Giant_Salted_Fish
 */
public class ModuleWrapper implements IModifiable, ICapabilityProvider, IAutowireSmoother
{
	/**
	 * A default wrapper that do nothing on {@link #syncNBTData()}. Can be used to initialize your
	 * newly created {@link IModifiable} or when it is being removed so that setter methods can work
	 * fine before it is installed onto a real module/wrapper.
	 */
	public static final ModuleWrapper DEFAULT = new ModuleWrapper() {
		@Override
		public void syncNBTData() { }
	};
	
	protected static final String NAME = "_BASE_WRAPPER";
	
	protected static final SimpleSlot SLOT = new SimpleSlot();
	
	protected transient final Supplier< NBTTagCompound > nbt;
	
	protected transient IModifiable primary;
	
	protected transient final Mat4f mat = new Mat4f();
	
	/**
	 * {@link #syncNBTData()} not called. Call it after this if it is needed.
	 */
	public ModuleWrapper( Supplier< NBTTagCompound > nbt, IModifiable primary )
	{
		this.nbt = nbt;
		
		primary.setBase( this, 0 );
		this.primary = primary;
	}
	
	protected ModuleWrapper() { this.nbt = null; }
	
	@Override
	public boolean hasCapability( Capability< ? > capability, @Nullable EnumFacing facing ) {
		return capability == CAPABILITY;
	}
	
	@Override
	public < T > T getCapability( Capability< T > capability, @Nullable EnumFacing facing ) {
		return CAPABILITY.cast( this.primary );
	}
	
	@Override
	public String name() { return NAME; }
	
	@Override
	public String category() { return NAME; }
	
	@Override
	public ItemStack toStack() { throw new RuntimeException( "Try to get stack for " + this ); }
	
	@Override
	public IModifiable base() { return null; }
	
	@Override
	public void setBase( IModifiable base, int baseSlot ) {
		throw new RuntimeException( "Try to set base for " + this );
	}
	
	@Override
	public void forEach( Consumer< IModifiable > visitor ) { this.primary.forEach( visitor ); }
	
	@Override
	public void install( int slot, IModifiable module )
	{
		this.primary = module;
		module.setBase( this, 0 );
		this.syncNBTData();
	}
	
	@Override
	public ModifyPredication tryInstallPreview( int slot, IModifiable module ) {
		throw new RuntimeException( "Try to install preview onto " + this );
	}
	
	@Override
	public ModifyPredication checkInstalledPosition( IModifiable installed ) {
		return ModifyPredication.OK;
	}
	
	@Override
	public IModifiable remove( int slot, int idx ) {
		throw new RuntimeException( "Try to remove module from " + this );
	}
	
	@Override
	public void onBeingInstalled( IModifiable base, int baseSlot ) {
		throw new RuntimeException( "Try to install " + this );
	}
	
	@Override
	public IModifiable onBeingRemoved() {
		throw new RuntimeException( "Try to remove " + this );
	}
	
	@Override
	public IModifiable getInstalled( int slot, int idx ) { return this.primary; }
	
	@Override
	public int getInstalledCount( int slot ) { return 1; }
	
	@Override
	public int slotCount() { return 1; }
	
	@Override
	public IModuleSlot getSlot( int idx ) { return SLOT; }
	
	@Override
	public int step() { throw new RuntimeException( "Try to get step from " + this ); }
	
	@Override
	public void $step( int step ) { throw new RuntimeException( "Try to set step for " + this ); }
	
	@Override
	public int offsetCount() {
		throw new RuntimeException( "Try to get offset count from " + this );
	}
	
	@Override
	public int offset() { throw new RuntimeException( "Try to get offset from " + this ); }
	
	@Override
	public void $offset( int offset ) {
		throw new RuntimeException( "Try to set offset for " + this );
	}
	
	@Override
	public int paintjobCount() {
		throw new RuntimeException( "Try to get paintjob count from " + this );
	}
	
	@Override
	public int paintjob() {
		throw new RuntimeException( "Try to get paintjob from " + this );
	}
	
	@Override
	public void $paintjob( int paintjob ) {
		throw new RuntimeException( "Try to set paintjob for " + this );
	}
	
	@Override
	public ModifyState modifyState() {
		throw new RuntimeException( "Try to get modify state from " + this );
	}
	
	@Override
	public void $modifyState( ModifyState state ) {
		throw new RuntimeException( "Try to set modify state for " + this );
	}
	
	@Override
	public void applyTransform( int slot, IModifiable module, Mat4f dst ) {
		Mat4f.mul( this.mat, dst, dst );
	}
	
	@Override
	@SideOnly( Side.CLIENT )
	public void prepareRenderer( Collection< IMultPassRenderer > renderQueue, IAnimator animator )
	{
		this.mat.setIdentity();
		animator.applyChannel( ItemAnimatorState.ITEM, this.smoother(), this.mat );
		this.primary.prepareRenderer( renderQueue, animator );
	}
	
//	@Override
//	@SideOnly( Side.CLIENT )
//	public IModifiable newModifyDelegate() {
//		throw new RuntimeException( "Try to new modify delegate for " + this );
//	}
	
	@Override
	@SideOnly( Side.CLIENT )
	public IModifiable newModifyIndicator() {
		throw new RuntimeException( "Try to new modify indicator for " + this );
	}
	
	@Override
	public void syncNBTData() {
		this.nbt.get().setTag( "_", this.primary.serializeNBT() );
	}
	
	@Override
	@SideOnly( Side.CLIENT )
	public void onReadNBTShareTag( NBTTagCompound nbt ) {
		this.primary.deserializeNBT( nbt.getCompoundTag( "_" ) );
	}
	
	@Override
	public NBTTagCompound serializeNBT() { return this.primary.serializeNBT(); }
	
	@Override
	public void deserializeNBT( NBTTagCompound nbt ) { this.primary.deserializeNBT( nbt ); }
}
