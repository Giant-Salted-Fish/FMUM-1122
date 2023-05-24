package com.fmum.common.player;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.MathHelper;

public abstract class TogglableOperation extends Operation
{
	protected IOperationController forwardController;
	protected IOperationController backwardController;
	
	protected TogglableOperation(
		IOperationController forwardController,
		IOperationController backwardController
	) {
		super( forwardController );
		
		this.forwardController = forwardController;
		this.backwardController = backwardController;
	}
	
	@Override
	public IOperation launch( EntityPlayer player )
	{
		// Use forward controller for launch.
		this.controller = this.forwardController;
		return this;
	}
	
	@Override
	public IOperation toggle( EntityPlayer player )
	{
		final boolean isForward = this.controller == this.forwardController;
		this.controller = isForward ? this.backwardController : this.forwardController;
		return this;
	}
	
	@Override
	public IOperation tick( EntityPlayer player )
	{
		final float progressor = this.controller.progressor();
		
		this.prevProgress = this.progress;
		this.progress = MathHelper.clamp( this.progress + progressor, 0F, 1F );
		// TODO: If it is needed to fire #onComplete() while the progress reaching 1F?
		
		// TODO: Handle sound and effect
		
		final boolean completed = progressor < 0F && this.prevProgress == 0F;
		return completed ? this.onComplete( player ) : this;
	}
}
