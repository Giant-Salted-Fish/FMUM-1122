package com.mcwb.common.operation;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.MathHelper;

public abstract class TogglableOperation< T > extends Operation< T >
{
	protected IOperationController forwardController;
	protected IOperationController backwardController;
	
	protected TogglableOperation(
		T equipped,
		IOperationController forwardController,
		IOperationController backwardController
	) {
		super( equipped, forwardController );
		
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
		final boolean isForwad = this.controller == this.forwardController;
		this.controller = isForwad ? this.backwardController : this.forwardController;
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
		return completed ? NONE : this;
	}
}
