package com.fmum.client;

import com.fmum.common.Meta;

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
	 * @return {@code this}
	 */
	public default Operation launch( ItemStack stack ) { return this; }
	
	/**
	 * Tick this operation
	 * 
	 * @param stack Current holding stack
	 * @return {@link #NONE} if this operation has complete. {@code this} otherwise.
	 */
	public default Operation tick( ItemStack stack ) { return this; }
	
	/**
	 * A new operation is arriving. Call {@link #launch(ItemStack)} on the given {@link Operation}
	 * with given {@link ItemStack} if this operation should terminate its execution to let the new
	 * operation to run.
	 * 
	 * @param op New operation
	 * @return {@link Operation} that should be execute after this call
	 */
	public default Operation onNewOpLaunch( Operation op, ItemStack stack ) {
		return op.launch( stack );
	}
	
	/**
	 * Called when player switch to a new holding item
	 * 
	 * @param stack New holding item
	 * @return {@link #NONE} if this operation should terminate on item switch
	 */
	public default Operation onHoldingItemChange( ItemStack stack ) { return NONE; }
	
	/**
	 * @return {@link #NONE} if this operation should terminate on GUI change
	 */
	public default Operation onGUIChange( GuiScreen gui ) { return NONE; }
	
	public default EntityPlayerSP getPlayer() { return FMUMClient.mc.player; }
}
