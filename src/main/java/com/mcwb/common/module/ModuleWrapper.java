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
	M extends IModular< ? extends M >,
	T extends IModular< ? extends M > & IPaintable
> implements IModular< M >, IPaintable, ICapabilityProvider
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
	public boolean hasCapability( Capability< ? > capability, @Nullable EnumFacing facing ) {
		return capability == CAPABILITY;
	}
	
	@Override
	public < C > C getCapability( Capability< C > capability, @Nullable EnumFacing facing ) {
		return CAPABILITY.cast( this );
	}
	
	@Override
	public String name() { return this.primary.name(); }
	
	@Override
	public String category() { return this.primary.category(); }
	
	// TODO: add proper error string. Also for sub-classes
	@Override
	public IModular< ? > base() { throw new RuntimeException(); }
	
	/**
	 * Set base is meaningless for wrapper so it is used here to reset wrapped primary
	 */
	@Override
	@SuppressWarnings( "unchecked" )
	public void setBase( IModular< ? > primary, int unused )
	{
		this.primary = ( T ) primary;
		primary.setBase( this, 0 );
	}
	
	@Override
	@SuppressWarnings( "unchecked" )
	public void postEvent( Object evt )
	{
		this.eventSubscribers.get( evt.getClass() ).forEach(
			subscriber -> ( ( Consumer< Object > ) subscriber ).accept( evt )
		);
	}
	
	@Override
	public void syncAndUpdate()
	{
		this.syncNBTData();
		this.eventSubscribers.clear();
		this.primary.updateState( this.eventSubscribers::put );
	}
	
	@Override
	public void updateState( BiConsumer< Class< ? >, IModuleEventSubscriber< ? > > registry ) {
		throw new RuntimeException();
	}
	
	@Override
	public IPreviewPredicate tryInstall( int slot, IModular< ? > module ) {
		throw new RuntimeException();
	}
	
	@Override
	public IModular< ? > doRemove( int slot, int idx ) { throw new RuntimeException(); }
	
	@Override
	public int install( int slot, IModular< ? > module ) { throw new RuntimeException(); }
	
	@Override
	public IModular< ? > remove( int slot, int idx ) { throw new RuntimeException(); }
	
	@Override
	public IModular< ? > onBeingInstalled() { return this.primary.onBeingInstalled(); }
	
	@Override
	public IModular< ? > onBeingRemoved() { throw new RuntimeException(); }
	
	@Override
	public IModifyPredicate checkHitboxConflict( IModular< ? > module ) {
		return IModifyPredicate.OK; // TODO: hitbox test
	}
	
	@Override
	public void forEach( Consumer< ? super M > visitor ) { this.primary.forEach( visitor ); }
	
	@Override
	public int getInstalledCount( int slot ) { throw new RuntimeException(); }
	
	@Override
	public M getInstalled( int slot, int idx ) { throw new RuntimeException(); }
	
	@Override
	public IModular< ? > getInstalled( byte[] loc, int locLen ) {
		return this.primary.getInstalled( loc, locLen );
	}
	
	@Override
	public void setInstalled( int slot, int idx, IModular< ? > module ) {
		throw new RuntimeException();
	}
	
	@Override
	public int slotCount() { throw new RuntimeException(); }
	
	@Override
	public IModuleSlot getSlot( int idx ) { throw new RuntimeException(); }
	
	@Override
	public int offsetCount() { throw new RuntimeException(); }
	
	@Override
	public int offset() { throw new RuntimeException(); }
	
	@Override
	public int step() { throw new RuntimeException(); }
	
	@Override
	public void setOffsetStep( int offset, int step ){ throw new RuntimeException(); }
	
	@Override
	public int paintjobCount() { throw new RuntimeException(); }
	
	@Override
	public int paintjob() { throw new RuntimeException(); }
	
	@Override
	public void setPaintjob( int paintjob ) { throw new RuntimeException(); }
	
	@Override
	public boolean tryOffer( int paintjob, EntityPlayer player ) {
		throw new RuntimeException();
	}
	
	@Override
	@SideOnly( Side.CLIENT )
	public boolean tryOfferOrNotifyWhy( int paintjob, EntityPlayer player ) {
		throw new RuntimeException();
	}
	
	@Override
	public IModifyState modifyState() { throw new RuntimeException(); }
	
	@Override
	public void setModifyState( IModifyState state ) { this.primary.setModifyState( state ); }
	
	@Override
	public void applyTransform( int slot, IModular< ? > module, Mat4f dst ) { }
	
	@Override
	@SideOnly( Side.CLIENT )
	public void prepareInHandRender(
		Collection< IDeferredRenderer > renderQueue0,
		Collection< IDeferredPriorityRenderer > renderQueue1,
		IAnimator animator
	) { throw new RuntimeException(); }
	
	@Override
	@SideOnly( Side.CLIENT )
	public void prepareRender(
		Collection< IDeferredRenderer > renderQueue0,
		Collection< IDeferredPriorityRenderer > renderQueue1,
		IAnimator animator
	) { throw new RuntimeException(); }
	
	@Override
	@SideOnly( Side.CLIENT )
	public IModular< ? > newModifyIndicator() { throw new RuntimeException(); }
	
	@Override
	public NBTTagCompound serializeNBT() { return this.primary.serializeNBT(); }
	
	@Override
	public void deserializeNBT( NBTTagCompound nbt ) { throw new RuntimeException(); }
	
	@Override
	public String toString() { return "Wrapper{" + this.primary + "}"; }
	
	protected abstract void syncNBTData();
}
