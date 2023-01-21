package com.mcwb.common.player;

import com.mcwb.client.MCWBClient;
import com.mcwb.common.item.IContextedItem;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public abstract class Operation< T extends IContextedItem > implements IOperation
{
	protected EntityPlayer player;
	protected T contexted;
	protected IOperationController controller;
	
	protected float prevProgress;
	protected float progress;
	
	protected Operation( EntityPlayer player, T contexted, IOperationController controller )
	{
		this.player = player;
		this.contexted = contexted;
		this.controller = controller;
	}
	
	@Override
	public float progress() { return this.progress; }
	
	@Override
	@SideOnly( Side.CLIENT )
	public float getProgress( float smoother ) {
		return this.prevProgress + ( this.progress - this.prevProgress ) * smoother;
	}
	
	@Override
	public IOperation tick()
	{
		// Update progress
		this.prevProgress = this.progress;
		this.progress = Math.min( 1F, this.progress + this.controller.progressor() );
		
		// TODO: handle effects and play sounds
		
		return this.prevProgress == 1F ? this.onComplete() : this;
	}
	
	@Override
	public IOperation onOtherTryLaunch( IOperation op ) { return this; }
	
	/**
	 * Called on {@link Side#CLIENT} to reuse the this instance
	 */
	@SideOnly( Side.CLIENT )
	public IOperation reset( T contexted )
	{
		this.player = MCWBClient.MC.player;
		this.contexted = contexted;
		return this;
	}
	
	protected IOperation onComplete() { return NONE; }
	
	protected void clearProgress()
	{
		this.prevProgress = 0F;
		this.progress = 0F;
	}
}
