package com.fmum.common.player;

import com.fmum.client.FMUMClient;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public abstract class Operation< T extends OperationController > implements IOperation
{
	protected T controller;
	
	protected float prevProgress;
	protected float progress;
	
	protected int currentEffect;
	protected int currentSound;
	
	protected Operation( T controller ) { this.controller = controller; }
	
	@Override
	public float progress() { return this.progress; }
	
	@Override
	@SideOnly( Side.CLIENT )
	public float smoothedProgress()
	{
		final float smoother = FMUMClient.MOD.smoother();
		return this.prevProgress + ( this.progress - this.prevProgress ) * smoother;
	}
	
	@Override
	public IOperation tick( EntityPlayer player )
	{
		// Update progress.
		final OperationController controller = this.controller;
		this.prevProgress = this.progress;
		this.progress = this.progress + controller.progressor();
		
		// Handle effects.
		final int effectCount = controller.effectCount();
		while ( this.currentEffect < effectCount )
		{
			final float effectTime = controller.getEffectTime( this.currentEffect );
			if ( effectTime > this.progress ) { break; }
			
			this.doHandleEffect( player );
			this.currentEffect += 1;
		}
		
		// Handle sounds.
		final int soundCount = controller.soundCount();
		while ( this.currentSound < soundCount )
		{
			final float soundTime = controller.getSoundTime( this.currentSound );
			if ( soundTime > this.progress ) { break; }
			
			controller.handlePlaySound( this.currentSound, player );
			this.currentSound += 1;
		}
		
		this.progress = Math.min( 1F, this.progress );
		return this.prevProgress == 1F ? this.onComplete( player ) : this;
	}
	
	@Override
	public IOperation onOtherTryLaunch( IOperation op, EntityPlayer player ) { return this; }
	
	@Override
	public String toString()
	{
		final String typeName = this.getClass().getTypeName();
		final String className = typeName.substring( typeName.lastIndexOf( '.' ) + 1 );
		return "Operation::" + className;
	}
	
	protected void clearProgress()
	{
		this.prevProgress = 0F;
		this.progress = 0F;
		this.currentEffect = 0;
		this.currentSound = 0;
	}
	
	protected IOperation onComplete( EntityPlayer player ) { return NONE; }
	
	/**
	 * Handle effect specified by {@link #currentEffect}.
	 */
	protected void doHandleEffect( EntityPlayer player ) { }
}
