package com.mcwb.common.modify;

import java.util.Collection;
import java.util.function.Consumer;

import com.mcwb.client.modify.IMultPassRenderer;
import com.mcwb.client.render.IAnimator;
import com.mcwb.common.item.HackedNBTTagCompound;
import com.mcwb.common.meta.IContexted;
import com.mcwb.util.Mat4f;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public interface IContextedModifiable extends IContexted, INBTSerializable< NBTTagCompound >
{
	// TODO: check if this is really needed
	@CapabilityInject( IContextedModifiable.class )
	public static final Capability< IContextedModifiable > CAPABILITY = null;
	
	public IModifiableMeta meta();
	
	public IContextedModifiable base();
	
	public int baseSlot();
	
	public void forEach( Consumer< IContextedModifiable > visitor );
	
	/**
	 * Check whether the given module can be installed into the given slot. Should at least perform
	 * compatibility check and capacity check.
	 */
	public boolean canInstall( int slot, IContextedModifiable module );
	
	public void install( int slot, IContextedModifiable module );
	
	public IContextedModifiable remove( int slot, int idx );
	
	/**
	 * Called before this module is actually installed on the base module
	 * 
	 * @see #setBase(IContextedModifiable, int)
	 * @param base The module that this module is being installed on
	 * @param baseSlot
	 *     Which slot that this module is installed into. Index is not provided as it could change
	 *     during the modification.
	 */
	public default IContextedModifiable onBeingInstalled(
		IContextedModifiable base,
		int baseSlot
	) {
		this.setBase( base, baseSlot );
		return this;
	}
	
	/**
	 * Called in {@link #deserializeNBT(NBTTagCompound)} to set its base and base slot. Should have
	 * no side effect rather than just setting the base as well as the base slot.
	 * 
	 * @see #onBeingInstalled(IContextedModifiable, int)
	 */
	public void setBase( IContextedModifiable base, int baseSlot );
	
	/**
	 * Called after this module is removed from its base module
	 * 
	 * @return Module that should be given back to player
	 */
	public IContextedModifiable onBeingRemoved();
	
	public IContextedModifiable getInstalled( int slot, int idx );
	
	// TODO: maybe provide a version of better performance
	public default IContextedModifiable getInstalled( byte[] loc, int locLen )
	{
		IContextedModifiable mod = this;
		for( int i = 0; i < locLen; i += 2 )
			mod = mod.getInstalled( 0xFF & loc[ i ], 0xFF & loc[ i + 1 ] );
		return mod;
	}
	
	public int getInstalledCount( int slot );
	
	public int step();
	
	public void $step( int step );
	
	public int offset();
	
	public void $offset( int offset );
	
	public int paintjob();
	
	public void $paintjob( int paintjob );
	
	public ModifyState modifyState();
	
	public void $modifyState( ModifyState state );
	
	public void getSlotTransform( IContextedModifiable installed, Mat4f dst );
	
	@SideOnly( Side.CLIENT )
	public void prepareRenderer( Collection< IMultPassRenderer > renderQueue, IAnimator animator );
	
//	public IModifiableMeta copy();
	
	/**
	 * <p> Simply return the bounden NBT tag. Should be the {@link HackedNBTTagCompound} stack tag
	 * if is primary base. </p>
	 * 
	 * <p> Do not directly feed the tag returned by this method to another context with
	 * {@link #deserializeNBT(NBTTagCompound)} as they will bind to the same tag. Copy the tag if
	 * that is needed. </p>
	 */
	@Override
	public NBTTagCompound serializeNBT();
	
	/**
	 * <p> Restore the state of the context with the given tag. </p>
	 * 
	 * <p> Notice that context will bind to the given NBT tag. </p>
	 * 
	 * @see #serializeNBT()
	 */
	@Override
	public void deserializeNBT( NBTTagCompound nbt );
	
	public enum ModifyState
	{
		NOT_SELECTED,
		SELECTED_OK,
		SELECTED_CONFLICT,
		AVOID_CONFLICT_CHECK;
	}
}
