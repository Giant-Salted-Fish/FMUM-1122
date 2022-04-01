package com.fmum.client;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Super type of operations that usually have a fixed amount of execution time
 * 
 * @author Giant_Salted_Fish
 */
@SideOnly(Side.CLIENT)
public abstract class OperationProgressive extends Operation
{
	public double progress = 0D;
	public double prevProgress = 0D;
	
	public double progressor = 1D / 16D;
	
	@Override
	public final double getProgress() { return this.progress; }
	
	@Override
	public final double getSmoothedProgress(float smoother) {
		return Math.min(1D, this.prevProgress + (this.progress - this.prevProgress) * smoother);
	} // TODO: remove Math.min when it is acceptable maybe?
	
	@Override
	protected void launch(ItemStack stack) { this.prevProgress = this.progress = 0D; }
	
	@Override
	protected boolean tick(ItemStack stack)
	{
		this.prevProgress = this.progress;
		if((this.progress += this.progressor) > 1D)
			this.progress = 1D;
		return this.prevProgress >= 1D;
	}
}
