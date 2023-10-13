package com.fmum.common.module;

import com.fmum.client.render.IAnimator;
import com.fmum.util.Category;
import com.fmum.util.Mat4f;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;

import java.util.Optional;
import java.util.function.Consumer;

public abstract class ModuleWrapper< T extends IModule > implements IModule
{
	protected T wrapped;
	
	protected ModuleWrapper( T wrapped )
	{
		this.wrapped = wrapped;
		wrapped._setBase( this, -1 );
	}
	
	@Override
	public final String name() {
		return this.wrapped.name();
	}
	
	@Override
	public final Category category() {
		return this.wrapped.category();
	}
	
	@Override
	public final int installationSlotIdx() {
		throw new RuntimeException();
	}
	
	@Override
	public final IModule base() {
		throw new RuntimeException();
	}
	
	@Override
	@SuppressWarnings( "unchecked" )
	public final void _setBase( IModule wrapped, int installation_slot_idx )
	{
		this.wrapped = ( T ) wrapped;
		wrapped._setBase( this, -1 );
	}
	
	@Override
	public final IModule _onBeingInstalled( IModule base, int base_slot_idx ) {
		return this.wrapped._onBeingInstalled( base, base_slot_idx );
	}
	
	@Override
	public final IModule _onBeingRemoved() {
		throw new RuntimeException();
	}
	
	@Override
	public final void forEachInstalled( Consumer< ? super IModule > visitor ) {
		this.wrapped.forEachInstalled( visitor );
	}
	
	@Override
	public final int getNumInstalledInSlot( int slot_idx ) {
		throw new RuntimeException();
	}
	
	@Override
	public final IModule getInstalled( byte[] idx_sequence, int sequence_len ) {
		throw new RuntimeException();
	}
	
	@Override
	public final IModule getInstalled( int slot_idx, int install_idx ) {
		throw new RuntimeException();
	}
	
	@Override
	public final int slotCount() {
		throw new RuntimeException();
	}
	
	@Override
	public final IModuleSlot getSlot( int idx ) {
		throw new RuntimeException();
	}
	
	@Override
	public final int offsetCount() {
		throw new RuntimeException();
	}
	
	@Override
	public final int offset() {
		throw new RuntimeException();
	}
	
	@Override
	public final int step() {
		throw new RuntimeException();
	}
	
	@Override
	public final int paintjobCount() {
		throw new RuntimeException();
	}
	
	@Override
	public final int paintjob() {
		throw new RuntimeException();
	}
	
	@Override
	public final Optional< Runnable > testAffordable( EntityPlayer player ) {
		throw new RuntimeException();
	}
	
	@Override
	public IModuleModifySession openModifySession() {
		// TODO: Is it ok to make modification start with wrapper?
		return this.wrapped.openModifySession();
	}
	
	@Override
	public void _getInstalledTransform(
		IModule installed,
		IAnimator animator,
		Mat4f dst
	) {
//		animator.getChannel( CHANNEL_ITEM, dst );
		dst.setIdentity();
	}
	
	@Override
	public final NBTTagCompound serializeNBT() {
		return this.wrapped.serializeNBT();
	}
	
	@Override
	public final void deserializeNBT( NBTTagCompound nbt ) {
		throw new RuntimeException();
	}
	
	@Override
	public String toString() {
		return String.format( "Wrapper{%s}", this.wrapped );
	}
}
