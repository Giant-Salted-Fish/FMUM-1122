package com.fmum.common.module;

import com.fmum.client.item.ItemModel;
import com.fmum.client.render.IAnimator;
import com.fmum.common.item.IItem;
import com.fmum.common.paintjob.IPaintable;
import com.fmum.util.Mat4f;
import com.google.common.collect.TreeMultimap;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Comparator;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Exists to satisfy the requirement of {@link IItem}. It forwards most method calls to the wrapped
 * primary module and should have the same behavior with the wrapped module when calling by outer.
 * 
 * @param <I> Type parameter of the wrapped module.
 * @param <T> Type of the primary that is wrapped.
 * @author Giant_Salted_Fish
 */
public abstract class ModuleWrapper<
	I extends IModule< ? extends I >,
	T extends IModule< ? extends I > & IPaintable
> implements IModule< I >, IPaintable, ICapabilityProvider
{
	protected transient final TreeMultimap< Class< ? >, IModuleEventSubscriber< ? > >
		eventSubscribers = TreeMultimap.create(
			( c0, c1 ) -> 0,
			Comparator.comparingInt( IModuleEventSubscriber::priority ) // TODO: receive event later with higher priority
		);
	
	protected transient T primary;
	
	protected ModuleWrapper( T primary )
	{
		this.primary = primary;
		primary.setBase( this, 0 );
	}
	
	@Override
	public final String name() { return this.primary.name(); }
	
	@Override
	public final ModuleCategory category() { return this.primary.category(); }
	
	@Override
	public int baseSlot() { throw new RuntimeException(); }
	
	// TODO: add proper error string. Also for sub-classes
	@Override
	public final IModule< ? > base() { throw new RuntimeException(); }
	
	/**
	 * Set base is meaningless for wrapper. So it is used here to reset wrapped primary.
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
		this.primary.updateModuleState( this.eventSubscribers::put );
	}
	
	@Override
	public final void updateModuleState(
		BiConsumer< Class< ? >, IModuleEventSubscriber< ? > > registry
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
	public final void forEachModule( Consumer< ? super I > visitor ) { this.primary.forEachModule( visitor ); }
	
	@Override
	public final int getInstalledCount( int slot ) { throw new RuntimeException(); }
	
	@Override
	public final I getInstalled( int slot, int idx ) { throw new RuntimeException(); }
	
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
	public final void setModifyState( IModifyState state ) { throw new RuntimeException(); }
	
	@Override
	public final void getTransform( IModule< ? > installed, Mat4f dst ) {
		dst.setIdentity();
	}
	
	@Override
	@SideOnly( Side.CLIENT )
	public final void getRenderTransform( IModule< ? > installed, IAnimator animator, Mat4f dst ) {
		animator.getChannel( ItemModel.CHANNEL_ITEM, dst );
	}
	
//	@Override
//	@SideOnly( Side.CLIENT )
//	public final void prepareRender(
//		IAnimator animator,
//		Collection< IDeferredRenderer > renderQueue0,
//		Collection< IDeferredPriorityRenderer > renderQueue1
//	) { throw new RuntimeException(); }
	
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
