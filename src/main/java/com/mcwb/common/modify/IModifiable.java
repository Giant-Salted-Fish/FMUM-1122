package com.mcwb.common.modify;

import java.util.Collection;
import java.util.function.Consumer;

import com.mcwb.client.modify.IDeferredPriorityRenderer;
import com.mcwb.client.modify.IDeferredRenderer;
import com.mcwb.client.render.IAnimator;
import com.mcwb.common.meta.IContexted;
import com.mcwb.util.Mat4f;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public interface IModifiable extends IContexted, INBTSerializable< NBTTagCompound >
{
	public String name();
	
	public String category();
	
	/**
	 * You should not use this context any more after calling this method 
	 */
	public ItemStack toStack();
	
	public IModifiable base();
	
	public void setBase( IModifiable base, int baseSlot );
	
	public void forEach( Consumer< IModifiable > visitor );
	
	public void install( int slot, IModifiable module );
	
	public ModifyPredication tryInstallPreview( int slot, IModifiable module );
	
	public ModifyPredication checkInstalledPosition( IModifiable installed );
	
	public IModifiable remove( int slot, int idx );
	
	public default void onBeingInstalled( IModifiable base, int baseSlot ) {
		this.setBase( base, baseSlot );
	}
	
	public void onBeingRemoved();
	
	public IModifiable getInstalled( int slot, int idx );
	
	// TODO: maybe provide a version of better performance
	public default IModifiable getInstalled( byte[] loc, int locLen )
	{
		IModifiable module = this;
		for( int i = 0; i < locLen; i += 2 )
			module = module.getInstalled( 0xFF & loc[ i ], 0xFF & loc[ i + 1 ] );
		return module;
	}
	
	public int getInstalledCount( int slot );
	
	public int slotCount();
	
	public IModuleSlot getSlot( int idx );
	
	public int step();
	
	public void $step( int step );
	
	public int offsetCount();
	
	public int offset();
	
	public void $offset( int offset );
	
	public int paintjobCount();
	
	public int paintjob();
	
	public void $paintjob( int paintjob );
	
	public IModifyState modifyState();
	
	public void $modifyState( IModifyState state );
	
	public void applyTransform( int slot, IModifiable module, Mat4f dst );
	
	@SideOnly( Side.CLIENT )
	public void prepareHandRender(
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
	 * Called on primary upon module installation, module removal and after deserialization
	 */
	public default void updatePrimaryState() { }
	
	/**
	 * Called to guarantee the changes will be updated to the stack tag
	 */
	public default void syncNBTData() { this.base().syncNBTData(); }
	
	/**
	 * @see Item#readNBTShareTag(ItemStack, NBTTagCompound)
	 */
	@SideOnly( Side.CLIENT )
	public default void onReadNBTShareTag( NBTTagCompound nbt ) {
		this.base().onReadNBTShareTag( nbt );
	}
	
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
	 * <p> Notice that context will bind to the given NBT tag. And this will not invoke
	 * {@link #syncNBTData()}. Call it after this if that is required. </p>
	 * 
	 * @see #serializeNBT()
	 */
	@Override
	public void deserializeNBT( NBTTagCompound nbt );
	
	@SideOnly( Side.CLIENT )
	public IModifiable newModifyIndicator();
}
