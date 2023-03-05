package com.mcwb.common.operation;

import com.mcwb.common.meta.IContexted;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public abstract class Operation< T extends IContexted > implements IOperation
{
	protected EntityPlayer player;
	protected T contexted;
	protected IOperationController controller;
	
	protected float prevProgress;
	protected float progress;
	
	protected int ieffect;
	protected int isound;
	
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
	public IOperation launch( IOperation oldOp )
	{
		this.clearProgress(); // TODO: if this is necessary?
		this.ieffect = 0;
		this.isound = 0;
		return this;
	}
	
	@Override
	public IOperation terminate()
	{
		this.clearProgress(); // TODO: if this is necessary?
		return NONE;
	}
	
	@Override
	public IOperation tick()
	{
		// Update progress
		this.prevProgress = this.progress;
		this.progress = Math.min( 1F, this.progress + this.controller.progressor() );
		
		// Handle effects
		for(
			final int effectCount = this.controller.effectCount();
			this.ieffect < effectCount
				&& this.controller.getEffectTime( this.ieffect ) <= this.progress;
			++this.ieffect
		) this.dohandleEffect();
		
		// Handle sounds
		for(
			final int soundCount = this.controller.soundCount();
			this.isound < soundCount
				&& this.controller.getSoundTime( this.isound ) <= this.progress;
			++this.isound
		) this.controller.handlePlaySound( this.isound, this.player );
		
		return this.prevProgress == 1F ? this.onComplete() : this;
	}
	
	@Override
	public IOperation onOtherTryLaunch( IOperation op ) { return this; }
	
	@Override
	public String toString() { return "Operation::" + this.getClass().getTypeName(); }
	
	protected void clearProgress()
	{
		this.prevProgress = 0F;
		this.progress = 0F;
	}
	
	protected IOperation onComplete() { return NONE; }
	
	/**
	 * Handle effect specified by {@link #ieffect}
	 */
	protected void dohandleEffect() { }
}
