package com.mcwb.common.player;

import com.mcwb.common.gun.IGun;
import com.mcwb.common.gun.IMag;
import com.mcwb.common.item.IItem;
import com.mcwb.common.item.IItemTypeHost;
import com.mcwb.common.operation.IOperation;
import com.mcwb.common.operation.Operation;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

public class OpLoadMag extends Operation< IGun >
{
	protected final int invSlot;
	
	public OpLoadMag( EntityPlayer player, IGun contexted, int invSlot )
	{
		super( player, contexted, contexted.loadMagController() );
		
		this.invSlot = invSlot;
	}
	
	@Override
	public IOperation launch( IOperation oldOp )
	{
		switch( 0 )
		{
		default:
			if( this.contexted.hasMag() ) break;
			
			final ItemStack stack = this.player.inventory.getStackInSlot( this.invSlot );
			final IItem item = IItemTypeHost.getType( stack ).getContexted( stack );
			final boolean isMag = item instanceof IMag;
			if( !isMag || this.contexted.isAllowed( ( IMag ) item ) ) break;
			
			return super.launch( oldOp );
		}
		return NONE;
	}
	
	@Override
	public IOperation onHoldingStackChange( IItem newItem )
	{
		this.contexted = ( IGun ) newItem;
		return this.contexted.hasMag() ? this.terminate() : this;
	}
	
	@Override
	protected void dohandleEffect()
	{
		final ItemStack stack = this.player.inventory.getStackInSlot( this.invSlot );
		final IItem item = IItemTypeHost.getType( stack ).getContexted( stack );
		final boolean isMag = item instanceof IMag;
		if( !isMag ) return;
		
		final IMag mag = ( IMag ) item;
		if( !this.contexted.isAllowed( mag ) ) return;
		
		this.contexted.loadMag( mag );
		stack.shrink( 1 );
	}
}
