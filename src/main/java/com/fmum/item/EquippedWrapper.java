package com.fmum.item;

import com.fmum.input.IInput;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Optional;

public abstract class EquippedWrapper implements IMainEquipped
{
	public final IMainEquipped wrapped;
	
	protected EquippedWrapper( IMainEquipped wrapped ) {
		this.wrapped = wrapped;
	}
	
	@Override
	public abstract IMainEquipped tickInHand( IItem item, EntityPlayer player );
	
	@Override
	public Optional< IMainEquipped > tickPutAway( IItem item, EntityPlayer player ) {
		return this.wrapped.tickPutAway( item, player );
	}
	
	@Override
	@SideOnly( Side.CLIENT )
	public void prepareRenderInHand( IItem item ) {
		this.wrapped.prepareRenderInHand( item );
	}
	
	@Override
	@SideOnly( Side.CLIENT )
	public boolean renderInHand( IItem item ) {
		return this.wrapped.renderInHand( item );
	}
	
	@Override
	@SideOnly( Side.CLIENT )
	public boolean renderSpecificInHand( IItem item ) {
		return this.wrapped.renderSpecificInHand( item );
	}
	
	@Override
	@SideOnly( Side.CLIENT )
	public boolean onMouseWheelInput( int dwheel, IItem item ) {
		return this.wrapped.onMouseWheelInput( dwheel, item );
	}
	
	@Override
	@SideOnly( Side.CLIENT )
	public abstract IMainEquipped onInputUpdate( String name, IInput input, IItem item );
	
	@Override
	@SideOnly( Side.CLIENT )
	public boolean shouldDisableCrosshair( IItem item ) {
		return this.wrapped.shouldDisableCrosshair( item );
	}
	
	@Override
	@SideOnly( Side.CLIENT )
	public boolean getViewBobbing( boolean original, IItem item ) {
		return this.wrapped.getViewBobbing( original, item );
	}
	
	@Override
	@SideOnly( Side.CLIENT )
	public float getMouseSensitivity( float ori_sensi, IItem item ) {
		return this.wrapped.getMouseSensitivity( ori_sensi, item );
	}
}
