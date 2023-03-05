package com.mcwb.common.operation;

import com.mcwb.common.meta.IContexted;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.MathHelper;

public abstract class TogglableOperation< T extends IContexted > extends Operation< T >
{
	protected IOperationController forwardController;
	protected IOperationController backwardController;
	
	protected TogglableOperation(
		EntityPlayer player, T context,
		IOperationController forwardController,
		IOperationController backwardController
	) {
		super( player, context, forwardController );
		
		this.forwardController = forwardController;
		this.backwardController = backwardController;
	}
	
	@Override
	public IOperation launch( IOperation oldOp )
	{
		// Use forward controller for launch
		this.controller = this.forwardController;
		return super.launch( oldOp );
	}
	
	@Override
	public IOperation toggle()
	{
		this.controller = this.controller == this.forwardController
			? this.backwardController : this.forwardController;
		return this;
	}
	
	@Override
	public IOperation tick()
	{
		final float progressor = this.controller.progressor();
		
		this.prevProgress = this.progress;
		this.progress = MathHelper.clamp( this.progress + progressor, 0F, 1F );
		// TODO: If it is needed to fire #onComplete() while the progress reaching 1F?
		
		// TODO: Handle sound and effect
		
		return progressor < 0F && this.prevProgress == 0F ? NONE : this;
	}
}
