package com.fmum.client;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Super type of operations that usually have a fixed amount of execution time
 * 
 * @author Giant_Salted_Fish
 */
@SideOnly( Side.CLIENT )
public abstract class OpProgressive implements Operation
{
	public double progress = 0D;
	public double prevProgress = 0D;
	
	public double progressor = 1D / 16D;
	
	@Override
	public double progress() { return this.progress; }
	
	@Override
	public double smoothedProgress( float smoother ) {
		return Math.min( 1D, this.prevProgress + ( this.progress - this.prevProgress ) * smoother );
	}
	
	@Override
	public Operation launch( ItemStack stack )
	{
		this.prevProgress
			= this.progress
			= 0D;
		return this;
	}
	
	@Override
	public Operation tick( ItemStack stack )
	{
		this.prevProgress = this.progress;
		if( ( this.progress += this.progressor ) > 1D )
			this.progress = 1D;
		return this.prevProgress >= 1D ? NONE : this;
	}
}
