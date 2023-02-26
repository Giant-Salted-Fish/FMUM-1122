package com.mcwb.common.module;

import java.util.Collection;
import java.util.function.Consumer;

import com.mcwb.client.module.IDeferredPriorityRenderer;
import com.mcwb.client.module.IDeferredRenderer;
import com.mcwb.client.render.IAnimator;
import com.mcwb.common.meta.IContexted;
import com.mcwb.util.Mat4f;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * TODO: 实行完全的 cursor 模式？
 * For the default implementation, a module must have wrapper if it is not attached to any other
 * module.
 * 
 * @param <T> Modules installed on this will at least be a sub-type of this type
 * @author Giant_Salted_Fish
 */
public interface IModular< T extends IModular< ? extends T > >
	extends IContexted, INBTSerializable< NBTTagCompound >
{
	/**
	 * @see #getId(NBTTagCompound)
	 */
	public static final String DATA_TAG = "d";
	
	public String name();
	
	public String category();
	
	/**
	 * @return Primary module rather than the wrapper
	 */
	public IModular< ? > primary();
	
	public IModular< ? > base();
	
	public void setBase( IModular< ? > base, int baseSlot );
	
	/**
	 * Notice that for each will not visit itself
	 */
	public void forEach( Consumer< ? super T > visitor );
	
	/**
	 * You should call this to actually install a new module
	 * TODO: proper intro
	 */
	public void tryInstallTo( IModular< ? > base, int slot );
	
	/**
	 * You should call this to actually remove a installed module
	 * TODO: proper intro
	 */
	public IModular< ? > removeFromBase( int slot, int idx );
	
	/**
	 * Only use this in (@link #installTo(IModular, int)}
	 */
	public void install( int slot, IModular< ? > module );
	
	/**
	 * Only use this in {@link #removeFromBase()}
	 */
	public T remove( int slot, int idx );
	
	public T getInstalled( int slot, int idx );
	
	public int getInstalledCount( int slot );
	
	public int slotCount();
	
	public IModuleSlot getSlot( int idx );
	
	public int offsetCount();
	
	public int offset();
	
	public int step();
	
	public void setOffsetStep( int offset, int step );
	
	public void applyTransform( int slot, IModular< ? > module, Mat4f dst );
	
	@SideOnly( Side.CLIENT )
	public void prepareInHandRender(
		Collection< IDeferredRenderer > renderQueue0,
		Collection< IDeferredPriorityRenderer > renderQueue1,
		IAnimator animator
	);
	
	@SideOnly( Side.CLIENT )
	public void prepareRender(
		Collection< IDeferredRenderer > renderQueue0,
		Collection< IDeferredPriorityRenderer > renderQueue1,
		IAnimator animator
	);
	
	/**
	 * Call on primary after a change of the module tree structure to trigger state update. It it
	 * recommended to update your global matrix here if the slots' origin is fixed.
	 */
	public void updateState();
	
	public default void syncNBTData() { this.base().syncNBTData(); }
	
	/**
	 * <p> Simply return the bounden NBT tag. </p>
	 * 
	 * <p> Do not directly feed the tag returned by this method to another context with
	 * {@link #deserializeNBT(NBTTagCompound)} as they will bind to the same tag. Copy the tag if
	 * that is needed. </p>
	 */
	@Override
	public NBTTagCompound serializeNBT();
	
	/**
	 * <p> Restore the state of the context with the given tag. You should directly set the values
	 * rather than calling setting methods like {@link #$step(int)} as they may try to set the NBT
	 * data. </p>
	 * 
	 * <p> Notice that context will bind to the given NBT tag. This method call will not invoke
	 * {@link #syncNBTData()} and {@link #updateState()}. Call them after this if that is required.
	 * </p>
	 * 
	 * @see #serializeNBT()
	 */
	@Override
	public void deserializeNBT( NBTTagCompound nbt );
	
	/**
	 * <p> This is the standard method to get id from the any given module tag. Make sure your
	 * implementation is compatible with this method to guarantee your module will be deserialized
	 * correctly when it is installed onto some other modules that are not provided by you as they
	 * can not make a prediction of how to retrieve id from your module tag. </p>
	 * 
	 * <p> For similar reason you should use this method to retrieve id from the data tag of the
	 * modules installed on your module to ensure compatibility. </p>
	 */
	public static int getId( NBTTagCompound tag ) {
		return 0xFFFF & tag.getIntArray( DATA_TAG )[ 0 ];
	}
}
