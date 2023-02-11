package com.mcwb.common.modify;

import java.util.Collection;
import java.util.function.Consumer;

import com.mcwb.client.IAutowireBindTexture;
import com.mcwb.client.modify.ISecondaryRenderer;
import com.mcwb.client.render.IAnimator;
import com.mcwb.client.render.IRenderer;
import com.mcwb.client.render.Renderer;
import com.mcwb.common.meta.IContexted;
import com.mcwb.util.Mat4f;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
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
	
	// TODO: check if this is needed?
	public void install( int slot, IModifiable module );
	
	public ModifyPredication tryInstallPreview( int slot, IModifiable module );
	
	public ModifyPredication checkInstalledPosition( IModifiable installed );
	
	public IModifiable remove( int slot, int idx );
	
	public default void onBeingInstalled( IModifiable base, int baseSlot ) {
		this.setBase( base, baseSlot );
	}
	
	public IModifiable onBeingRemoved();
	
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
	
	public ModifyState modifyState();
	
	public void $modifyState( ModifyState state );
	
	public void applyTransform( int slot, IModifiable module, Mat4f dst );
	
	@SideOnly( Side.CLIENT )
	public void prepareHandRenderer(
		Collection< IRenderer > renderQueue,
		Collection< ISecondaryRenderer > secondaryRenderQueue,
		IAnimator animator
	);
	
	@SideOnly( Side.CLIENT )
	public void prepareRenderer(
		Collection< IRenderer > renderQueue,
		Collection< ISecondaryRenderer > secondaryRenderQueue,
		IAnimator animator
	);
	
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
	
	// TODO: seems that we do not switch on this so make it a normal class rather than enum
	public static enum ModifyState implements IAutowireBindTexture
	{
		NOT_SELECTED,
		SELECTED_OK
		{
			@Override
			@SideOnly( Side.CLIENT )
			public void doRecommendedRender( ResourceLocation texture, IRenderer renderer )
			{
				Renderer.glowOn();
				this.bindTexture( Renderer.TEXTURE_GREEN );
				renderer.render();
				Renderer.glowOff();
			}
		},
		SELECTED_CONFLICT
		{
			@Override
			@SideOnly( Side.CLIENT )
			public void doRecommendedRender( ResourceLocation texture, IRenderer renderer )
			{
				Renderer.glowOn();
				this.bindTexture( Renderer.TEXTURE_RED );
				renderer.render();
				Renderer.glowOff();
			}
		},
		AVOID_CONFLICT_CHECK;
		
		/**
		 * Bind recommended texture, do setup work and call render
		 */
		@SideOnly( Side.CLIENT )
		public void doRecommendedRender( ResourceLocation texture, IRenderer renderer )
		{
			this.bindTexture( texture );
			renderer.render();
		}
	}
}
