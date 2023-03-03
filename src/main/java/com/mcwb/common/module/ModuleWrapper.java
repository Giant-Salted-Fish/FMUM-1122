package com.mcwb.common.module;

import java.util.Collection;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import com.mcwb.client.module.IDeferredPriorityRenderer;
import com.mcwb.client.module.IDeferredRenderer;
import com.mcwb.client.render.IAnimator;
import com.mcwb.common.item.IItem;
import com.mcwb.common.paintjob.IPaintable;
import com.mcwb.util.Mat4f;

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
	protected T primary;
	
	protected ModuleWrapper( IModular< ? > primary ) { this.setBase( primary, 0 ); }
	
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
	
	// TODO: check if this is valid?
	@Override
	public IModular< ? > primary() { return this.primary; }
	
	/**
	 * Get base on primary to actually get wrapper
	 */
	@Override
	public IModular< ? > base() { throw new RuntimeException( "Try to get base from " + this ); }
	
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
	public void forEach( Consumer< ? super M > visitor ) { this.primary.forEach( visitor ); }
	
	@Override
	public void syncAndUpdate()
	{
		this.primary.updateState();
		this.syncNBTData();
	}
	
	@Override
	public void updateState() {
		throw new RuntimeException( "Try to call update state on " + this );
	}
	
	@Override
	public IModifyPredicate tryInstallTo( IModular< ? > base, int slot ) {
		throw new RuntimeException( "Try to call try install to on " + this );
	}
	
	@Override
	public IModular< ? > removeFromBase( int slot, int idx ) {
		throw new RuntimeException( "Try to call remove from base on " + this );
	}
	
	@Override
	public IModuleModifier onModuleInstall(
		IModular< ? > base, int slot, IModular< ? > module,
		IModuleModifier modifier
	) {
		//if( modifier.predicate().ok() ) // TODO: If {module == this} is required?
		
		// Avoid install wrapper
		return this.primary.onModuleInstall( base, slot, module, () -> {
			base.install( slot, this.primary );
			return this.primary;
		} );
	}
	
	@Override
	public IModuleModifier onModuleRemove(
		IModular< ? > base, int slot, int idx,
		IModuleModifier modifier
	) { throw new RuntimeException( "Try to call on module remove on " + this ); }
	
	// TODO: validate if these methods are actually called
	@Override
	public void install( int slot, IModular< ? > module ) { this.primary.install( slot, module ); }
	
	@Override
	public M remove( int slot, int idx ) { return this.primary.remove( slot, idx ); }
	
	@Override
	public M getInstalled( int slot, int idx ) { return this.primary.getInstalled( slot, idx ); }
	
	@Override
	public int getInstalledCount( int slot ) { return this.primary.getInstalledCount( slot ); }
	
	@Override
	public int slotCount() { return this.primary.slotCount(); }
	
	@Override
	public IModuleSlot getSlot( int idx ) { return this.primary.getSlot( idx ); }
	
	// TODO: if get and set offset and step are valid 
	@Override
	public int offsetCount() { return this.primary.offsetCount(); }
	
	@Override
	public int offset() { return this.primary.offset(); }
	
	@Override
	public int step() { return this.primary.step(); }
	
	@Override
	public void updateOffsetStep( int offset, int step ) {
		this.primary.updateOffsetStep( offset, step );
	}
	
	@Override
	public int paintjobCount() { return this.primary.paintjobCount(); }
	
	@Override
	public int paintjob() { return this.primary.paintjob(); }
	
	@Override
	public void updatePaintjob( int paintjob ) { this.primary.updatePaintjob( paintjob ); }
	
	@Override
	public IModifyState modifyState() {
		throw new RuntimeException( "Try to get modify state from wrapper" );
	}
	
	@Override
	public void setModifyState( IModifyState state ) {
		throw new RuntimeException( "Try to set modify state for wrapper" );
	}
	
	/**
	 * This is on purpose to simply the matrix update of the module tree
	 */
	@Override
	public void applyTransform( int slot, IModular< ? > module, Mat4f dst ) { }
	
	@Override
	@SideOnly( Side.CLIENT )
	public void prepareInHandRender(
		Collection< IDeferredRenderer > renderQueue0,
		Collection< IDeferredPriorityRenderer > renderQueue1,
		IAnimator animator
	) { throw new RuntimeException( "Try to call prepare in hand render on wrapper" ); }
	
	@Override
	@SideOnly( Side.CLIENT )
	public void prepareRender(
		Collection< IDeferredRenderer > renderQueue0,
		Collection< IDeferredPriorityRenderer > renderQueue1,
		IAnimator animator
	) { throw new RuntimeException( "Try to call prepare render on wrapper" ); }
	
	@Override
	public NBTTagCompound serializeNBT() { return this.primary.serializeNBT(); }
	
	@Override
	public void deserializeNBT( NBTTagCompound nbt ) {
		throw new RuntimeException( "Try to call deserialize NBT on wrapper" );
	}
	
	@Override
	public String toString() { return "Wrapper{" + this.primary + "}"; }
	
	protected abstract void syncNBTData();
}
