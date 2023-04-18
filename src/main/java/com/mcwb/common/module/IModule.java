package com.mcwb.common.module;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

import com.mcwb.util.Mat4f;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public interface IModule< T extends IModule< ? extends T > >
	extends INBTSerializable< NBTTagCompound >
{
	/**
	 * @see #getId(NBTTagCompound)
	 */
	public static final String DATA_TAG = "d";
	
	public String name();
	
	public String category();
	
	/**
	 * @return An {@link ItemStack} that linked to this module.
	 */
	public ItemStack toStack();
	
	public int baseSlot();
	
	public IModule< ? > base();
	
	public void setBase( IModule< ? > base, int baseSlot );
	
	public void postEvent( Object evt );
	
	/**
	 * Synchronize NBT tag data and do update.
	 * 
	 * @see #updateState(BiConsumer)
	 */
	public void syncAndUpdate();
	
	/**
	 * Used in {@link #syncAndUpdate()}.
	 */
	public void updateState( BiConsumer< Class< ? >, IModuleEventSubscriber< ? > > registry );
	
	public IPreviewPredicate tryInstall( int slot, IModule< ? > module );
	
	public IModule< ? > doRemove( int slot, int idx );
	
	/**
	 * <p> Simply install the given module without posting the event. </p>
	 * 
	 * <p> In most cases you should use {@link #tryInstall(int, IModule)} rather than this to
	 * install a new module. </p>
	 * 
	 * @return The actual index of the installed module in given slot.
	 */
	public int install( int slot, IModule< ? > module );
	
	/**
	 * <p> Simply remove the given module without posting the event. </p>
	 * 
	 * <p> In most cases you should use {@link #doRemove(int, int)} rather this to remove an
	 * installed module. </p>
	 */
	public IModule< ? > remove( int slot, int idx );
	
	public IModule< ? > onBeingInstalled();
	
	public IModule< ? > onBeingRemoved();
	
	// TODO: maybe always copy before testing the hit box as #mat may be used for render in client side
	public IModifyPredicate checkHitboxConflict( IModule< ? > module );
	
	/**
	 * Notice that for each will not visit itself.
	 */
	public void forEach( Consumer< ? super T > visitor );
	
	public int getInstalledCount( int slot );
	
	public T getInstalled( int slot, int idx );
	
	public IModule< ? > getInstalled( byte[] loc, int locLen );
	
	public void setInstalled( int slot, int idx, IModule< ? > module );
	
	public int slotCount();
	
	public IModuleSlot getSlot( int idx );
	
	public int offsetCount();
	
	public int offset();
	
	public int step();
	
	public void setOffsetStep( int offset, int step );
	
	public IModifyState modifyState();
	
	public void setModifyState( IModifyState state );
	
	public void getTransform( IModule< ? > installed, Mat4f dst );
	
	@SideOnly( Side.CLIENT )
	public void getRenderTransform( IModule< ? > installed, Mat4f dst );
	
//	@SideOnly( Side.CLIENT )
//	public void prepareRender(
//		IAnimator animator,
//		Collection< IDeferredRenderer > renderQueue0,
//		Collection< IDeferredPriorityRenderer > renderQueue1
//	);
	
	@SideOnly( Side.CLIENT )
	public IModule< ? > newModifyIndicator();
	
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
	 * rather than calling setting methods like {@link #setOffsetStep(int)} as they may try to set
	 * the NBT data. </p>
	 * 
	 * <p> Notice that context will bind to the given NBT tag. </p>
	 * 
	 * <p> This method call will not invoke {@link #syncAndUpdate()}. </p>
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
