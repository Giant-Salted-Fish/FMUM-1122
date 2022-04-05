package com.fmum.client;

import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class Operation
{
	public static final Operation NONE = new Operation();
	
	public double getProgress() { return this.getSmoothedProgress(1F); }
	
	public double getSmoothedProgress(float smoother) { return 1D; }
	
	/**
	 * Tick this operation
	 * 
	 * @param stack Current holding stack
	 * @return {@code true} if this operation has complete and should exit
	 */
	protected boolean tick(ItemStack stack) { return false; }
	
	/**
	 * Launch this operation with given holding stack
	 * 
	 * @param stack ItemStack that the player is currently holding
	 */
	protected void launch(ItemStack stack) { }
	
	/**
	 * A new operation is arriving, check if this operation volunteer to terminate itself so the
	 * new operation can execute immediately
	 * 
	 * @param op New operation
	 * @return {@code true} if this operation abandon its execution and let new operation to run
	 */
	protected boolean encounter(Operation op) { return true; }
	
	/**
	 * Called when player switch to a new holding item
	 * 
	 * @param stack New holding item
	 * @return {@code true} if should give up execution of this operation
	 */
	protected boolean switchItem(ItemStack stack) { return true; }
	
	/**
	 * @return {@code true} if should kill this operation on GUI change
	 */
	protected boolean onGUIChange(GuiScreen gui) { return true; }
	
	protected static EntityPlayerSP getPlayer() { return FMUMClient.mc.player; }
}
