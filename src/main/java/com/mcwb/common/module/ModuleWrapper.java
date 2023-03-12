package com.mcwb.common.module;

import java.util.Collection;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import com.google.common.collect.TreeMultimap;
import com.mcwb.client.module.IDeferredPriorityRenderer;
import com.mcwb.client.module.IDeferredRenderer;
import com.mcwb.client.render.IAnimator;
import com.mcwb.common.item.IItem;
import com.mcwb.common.meta.IMeta;
import com.mcwb.common.paintjob.IPaintable;
import com.mcwb.util.Mat4f;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Exists to satisfy the requirement of {@link IItem}. It forwards most method calls to the wrapped
 * primary module and should have the same behavior with the wrapped module when calling by outer.
 * 
 * @param <M> Type parameter of the wrapped module
 * @param <T> Type of the primary that this wraps
 * @author Giant_Salted_Fish
 */
public abstract class ModuleWrapper<
	M extends IModule< ? extends M >,
	T extends IModule< ? extends M > & IPaintable
> implements IModule< M >, IPaintable, ICapabilityProvider
{
	protected transient final TreeMultimap< Class< ? >, IModuleEventSubscriber< ? > >
		eventSubscribers = TreeMultimap.create(
			( c0, c1 ) -> 0,
			( s0, s1 ) -> s0.priority() - s1.priority() // TODO: receive event later with higher priority
		);
	
	protected transient T primary;
	
	protected ModuleWrapper( T primary )
	{
		this.primary = primary;
		primary.setBase( this, 0 );
	}
	
	@Override
	public final boolean hasCapability( Capability< ? > capability, @Nullable EnumFacing facing ) {
		return capability == IMeta.CONTEXTED;
	}
	
	@Override
	public final < C > C getCapability( Capability< C > capability, @Nullable EnumFacing facing ) {
		return IMeta.CONTEXTED.cast( this );
	}
	
	@Override
	public final String name() { return this.primary.name(); }
	
	@Override
	public final String category() { return this.primary.category(); }
	
	// TODO: add proper error string. Also for sub-classes
	@Override
	public final IModule< ? > base() { throw new RuntimeException(); }
	
	/**
	 * Set base is meaningless for wrapper so it is used here to reset wrapped primary
	 */
	@Override
	@SuppressWarnings( "unchecked" )
	public final void setBase( IModule< ? > primary, int unused )
	{
		this.primary = ( T ) primary;
		primary.setBase( this, 0 );
	}
	
	@Override
	@SuppressWarnings( "unchecked" )
	public final void postEvent( Object evt )
	{
		this.eventSubscribers.get( evt.getClass() ).forEach(
			subscriber -> ( ( Consumer< Object > ) subscriber ).accept( evt )
		);
	}
	
	@Override
	public final void syncAndUpdate()
	{
		this.syncNBTData();
		this.eventSubscribers.clear();
		this.primary.updateState( this.eventSubscribers::put );
	}
	
	@Override
	public final void updateState(
		BiConsumer< Class< ? >,
		IModuleEventSubscriber< ? > > registry
	) { throw new RuntimeException(); }
	
	@Override
	public final IPreviewPredicate tryInstall( int slot, IModule< ? > module ) {
		throw new RuntimeException();
	}
	
	@Override
	public final IModule< ? > doRemove( int slot, int idx ) { throw new RuntimeException(); }
	
	@Override
	public final int install( int slot, IModule< ? > module ) { throw new RuntimeException(); }
	
	@Override
	public final IModule< ? > remove( int slot, int idx ) { throw new RuntimeException(); }
	
	@Override
	public final IModule< ? > onBeingInstalled() { return this.primary.onBeingInstalled(); }
	
	@Override
	public final IModule< ? > onBeingRemoved() { throw new RuntimeException(); }
	
	@Override
	public final IModifyPredicate checkHitboxConflict( IModule< ? > module ) {
		return IModifyPredicate.OK; // TODO: hitbox test
	}
	
	@Override
	public final void forEach( Consumer< ? super M > visitor ) { this.primary.forEach( visitor ); }
	
	@Override
	public final int getInstalledCount( int slot ) { throw new RuntimeException(); }
	
	@Override
	public final M getInstalled( int slot, int idx ) { throw new RuntimeException(); }
	
	@Override
	public final IModule< ? > getInstalled( byte[] loc, int locLen ) {
		return this.primary.getInstalled( loc, locLen );
	}
	
	@Override
	public final void setInstalled( int slot, int idx, IModule< ? > module ) {
		throw new RuntimeException();
	}
	
	@Override
	public final int slotCount() { throw new RuntimeException(); }
	
	@Override
	public final IModuleSlot getSlot( int idx ) { throw new RuntimeException(); }
	
	@Override
	public final int offsetCount() { throw new RuntimeException(); }
	
	@Override
	public final int offset() { throw new RuntimeException(); }
	
	@Override
	public final int step() { throw new RuntimeException(); }
	
	@Override
	public final void setOffsetStep( int offset, int step ) { throw new RuntimeException(); }
	
	@Override
	public final int paintjobCount() { throw new RuntimeException(); }
	
	@Override
	public final int paintjob() { throw new RuntimeException(); }
	
	@Override
	public final void setPaintjob( int paintjob ) { throw new RuntimeException(); }
	
	@Override
	public final boolean tryOffer( int paintjob, EntityPlayer player ) {
		throw new RuntimeException();
	}
	
	@Override
	@SideOnly( Side.CLIENT )
	public final boolean tryOfferOrNotifyWhy( int paintjob, EntityPlayer player ) {
		throw new RuntimeException();
	}
	
	@Override
	public final IModifyState modifyState() { throw new RuntimeException(); }
	
	@Override
	public final void setModifyState( IModifyState state ) { this.primary.setModifyState( state ); }
	
	@Override
	public final void applyTransform( int slot, IModule< ? > module, Mat4f dst ) { }
	
	@Override
	@SideOnly( Side.CLIENT )
	public final void prepareRender(
		Collection< IDeferredRenderer > renderQueue0,
		Collection< IDeferredPriorityRenderer > renderQueue1,
		IAnimator animator
	) { throw new RuntimeException(); }
	
	@Override
	@SideOnly( Side.CLIENT )
	public final IModule< ? > newModifyIndicator() { throw new RuntimeException(); }
	
	@Override
	public final NBTTagCompound serializeNBT() { return this.primary.serializeNBT(); }
	
	@Override
	public final void deserializeNBT( NBTTagCompound nbt ) { throw new RuntimeException(); }
	
	@Override
	public String toString() { return "Wrapper{" + this.primary + "}"; }
	
	protected abstract void syncNBTData();
}
