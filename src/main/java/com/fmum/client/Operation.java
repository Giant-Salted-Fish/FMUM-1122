package com.fmum.client;

import com.fmum.common.FMUM;
import com.fmum.common.Meta;
import com.fmum.common.item.MetaItem;

import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly( Side.CLIENT )
public interface Operation extends Meta
{
	/**
	 * Default operation instance that simply do nothing
	 */
	public static final Operation NONE = new Operation() { };
	
	public default double progress() { return 1D; }
	
	public default double smoothedProgress( float smoother ) { return 1D; }
	
	/**
	 * Launch this operation with given holding stack
	 * 
	 * @param stack ItemStack that the player is currently holding
	 * @param meta Meta of the holding stack
	 * @return {@code this}
	 */
	public default Operation launch( ItemStack stack, MetaItem meta ) { return this; }
	
	/**
	 * Tick this operation
	 * 
	 * @param stack Current holding stack
	 * @param meta Meta of the holding stack
	 * @return {@link #NONE} if this operation has complete. {@code this} otherwise.
	 */
	public default Operation tick( ItemStack stack, MetaItem meta ) { return this; }
	
	/**
	 * A new operation is arriving. Call {@link #launch(ItemStack)} on the given {@link Operation}
	 * with given {@link ItemStack} if this operation should terminate its execution to let the new
	 * operation to run.
	 * 
	 * @param op New operation
	 * @param stack ItemStack that the player is currently holding
	 * @param meta Meta of the holding stack
	 * @return {@link Operation} that should be execute after this call
	 */
	public default Operation onNewOpLaunch( Operation op, ItemStack stack, MetaItem meta ) {
		return op.launch( stack, meta );
	}
	
	/**
	 * Called when player switch to a new holding item FIXME: add ori stack and meta maybe
	 * 
	 * @param stack New holding item
	 * @param meta Meta of the new holding item
	 * @return {@link #NONE} if this operation should terminate on item switch
	 */
	public default Operation onHoldingItemChange( ItemStack stack, MetaItem meta ) { return NONE; }
	
	/**
	 * @return {@link #NONE} if this operation should terminate on GUI change FIXME: add meta maybe
	 */
	public default Operation onGUIChange( GuiScreen gui ) { return NONE; }
	
	public default EntityPlayerSP getPlayer() { return FMUMClient.mc.player; }
	
	@Override
	public default String name() { return this.getClass().getCanonicalName(); }
	
	@Override
	public default String author() { return FMUM.MOD.author(); }
}
