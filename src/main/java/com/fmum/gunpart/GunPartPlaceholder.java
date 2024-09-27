package com.fmum.gunpart;

import com.fmum.item.ItemCategory;
import com.fmum.module.IModifyContext;
import com.fmum.module.IModifyPreview;
import com.fmum.module.IModule;
import com.fmum.render.IPreparedRenderer;
import com.fmum.render.ModelPath;
import com.mojang.realmsclient.util.Pair;
import gsf.util.animation.IAnimator;
import gsf.util.animation.IPoseSetup;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

@SideOnly( Side.CLIENT )
public abstract class GunPartPlaceholder implements IGunPart
{
	public static final ModelPath MODEL_PATH = new ModelPath( "fmum", "models/preview_placeholder.obj" );
	
	
	protected IGunPart base = null;
	
	@Override
	public ItemCategory getCategory() {
		return ItemCategory.parse( this.getClass().getSimpleName() );
	}
	
	@Override
	public Optional< ? extends IGunPart > getBase() {
		return Optional.ofNullable( this.base );
	}
	
	@Override
	public void IModule$setBase( IModule base ) {
		this.base = ( IGunPart ) base;
	}
	
	@Override
	public void IModule$clearBase() {
		this.base = null;
	}
	
	@Override
	public int countModuleInSlot( int slot_idx ) {
		return 0;
	}
	
	@Override
	public IGunPart getInstalled( int slot_idx, int module_idx ) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public int getSlotCount() {
		return 0;
	}
	
	@Override
	public int getPaintjobCount() {
		return 0;
	}
	
	@Override
	public int getPaintjobIdx() {
		return 0;
	}
	
	@Override
	public IModifyPreview< Integer > trySetPaintjob( int paintjob ) {
		return IModifyPreview.ok( () -> 0 );
	}
	
	@Override
	public IModifyPreview< Integer > tryInstall( int slot_idx, IModule module ) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public IModifyPreview< ? extends IModule > tryRemove( int slot_idx, int module_idx ) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public Pair< ? extends IModule, Supplier< ? extends IModule > > getModifyCursor(
		int slot_idx,
		int module_idx,
		IModifyContext ctx
	) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public int installPreviewPlaceholder( int slot_idx, IModifyContext ctx ) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public NBTTagCompound getBoundNBT() {
		return new NBTTagCompound();
	}
	
	@Override
	public ItemStack takeAndToStack() {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public int getOffsetCount() {
		return 1;
	}
	
	@Override
	public int getOffset() {
		return 0;
	}
	
	@Override
	public int getStep() {
		return 0;
	}
	
	@Override
	public int getStepCount( int slot_idx ) {
		return 1;
	}
	
	@Override
	public IModifyPreview< Pair< Integer, Integer > > trySetOffsetAndStep( int offset, int step ) {
		return IModifyPreview.ok( () -> Pair.of( 0, 0 ) );
	}
	
	@Override
	public IModule IGunPart$createSelectionProxy( IModifyContext ctx ) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public void IGunPart$prepareRender(
		int base_slot_idx,
		IAnimator animator,
		Consumer< IPreparedRenderer > registry
	) {
		final IPoseSetup setup = this.base.IGunPart$getRenderSetup( this, base_slot_idx );
		registry.accept( cam -> Pair.of( 0.0F, () -> this._renderModel( animator, setup ) ) );
	}
	
	protected abstract void _renderModel( IAnimator animator, IPoseSetup setup );
	
	@Override
	public IPoseSetup IGunPart$getRenderSetup( IGunPart gun_part, int slot_idx ) {
		throw new UnsupportedOperationException();
	}
}
