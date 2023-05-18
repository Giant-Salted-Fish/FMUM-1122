package com.fmum.common.module;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

import com.fmum.client.render.IAnimator;
import com.fmum.util.Mat4f;

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
	static final String DATA_TAG = "d";
	
	String name();
	
	ModuleCategory category();
	
	/**
	 * @return An {@link ItemStack} that linked to this module.
	 */
	ItemStack toStack();
	
	int baseSlot();
	
	IModule< ? > base();
	
	void setBase( IModule< ? > base, int baseSlot );
	
	void postEvent( Object evt );
	
	/**
	 * Synchronize NBT tag data and do update.
	 * 
	 * @see #updateModuleState(BiConsumer)
	 */
	void syncAndUpdate();
	
	/**
	 * Used in {@link #syncAndUpdate()}.
	 */
	void updateModuleState( BiConsumer< Class< ? >, IModuleEventSubscriber< ? > > registry );
	
	IPreviewPredicate tryInstall( int slot, IModule< ? > module );
	
	IModule< ? > doRemove( int slot, int idx );
	
	/**
	 * <p> Simply install the given module without posting the event. </p>
	 * 
	 * <p> In most cases you should use {@link #tryInstall(int, IModule)} rather than this to
	 * install a new module. </p>
	 * 
	 * @return The actual index of the installed module in given slot.
	 */
	int install( int slot, IModule< ? > module );
	
	/**
	 * <p> Simply remove the given module without posting the event. </p>
	 * 
	 * <p> In most cases you should use {@link #doRemove(int, int)} rather this to remove an
	 * installed module. </p>
	 */
	IModule< ? > remove( int slot, int idx );
	
	IModule< ? > onBeingInstalled();
	
	IModule< ? > onBeingRemoved();
	
	// TODO: maybe always copy before testing the hit box as #mat may be used for render in client side
	IModifyPredicate checkHitboxConflict( IModule< ? > module );
	
	/**
	 * Notice that for each will not visit itself.
	 */
	void forEachModule( Consumer< ? super T > visitor );
	
	int getInstalledCount( int slot );
	
	T getInstalled( int slot, int idx );
	
	IModule< ? > getInstalled( byte[] loc, int locLen );
	
	void setInstalled( int slot, int idx, IModule< ? > module );
	
	int slotCount();
	
	IModuleSlot getSlot( int idx );
	
	int offsetCount();
	
	int offset();
	
	int step();
	
	void setOffsetStep( int offset, int step );
	
	IModifyState modifyState();
	
	void setModifyState( IModifyState state );
	
	void getTransform( IModule< ? > installed, Mat4f dst );
	
	@SideOnly( Side.CLIENT )
	void getRenderTransform( IModule< ? > installed, IAnimator animator, Mat4f dst );
	
//	@SideOnly( Side.CLIENT )
//	void prepareRender(
//		IAnimator animator,
//		Collection< IDeferredRenderer > renderQueue0,
//		Collection< IDeferredPriorityRenderer > renderQueue1
//	);
	
	@SideOnly( Side.CLIENT )
	IModule< ? > newModifyIndicator();
	
	/**
	 * <p> Simply return the bounden NBT tag. </p>
	 * 
	 * <p> Do not directly feed the tag returned by this method to another context with
	 * {@link #deserializeNBT(NBTTagCompound)} as they will bind to the same tag. Copy the tag if
	 * that is needed. </p>
	 */
	@Override
	NBTTagCompound serializeNBT();
	
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
	void deserializeNBT( NBTTagCompound nbt );
	
	/**
	 * <p> This is the standard method to get id from the any given module tag. Make sure your
	 * implementation is compatible with this method to guarantee your module will be deserialized
	 * correctly when it is installed onto some other modules that are not provided by you as they
	 * can not make a prediction of how to retrieve id from your module tag. </p>
	 * 
	 * <p> For similar reason you should use this method to retrieve id from the data tag of the
	 * modules installed on your module to ensure compatibility. </p>
	 */
	static int getId( NBTTagCompound tag ) {
		return 0xFFFF & tag.getIntArray( DATA_TAG )[ 0 ];
	}
}
