package com.fmum.item;

import com.fmum.input.IInput;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumHand;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Optional;

public abstract class EquippedWrapper implements IEquippedItem
{
	public final IEquippedItem wrapped;
	
	protected EquippedWrapper( IEquippedItem wrapped ) {
		this.wrapped = wrapped;
	}
	
	@Override
	public abstract IEquippedItem tickInHand( IItem item, EnumHand hand, EntityPlayer player );
	
	@Override
	public Optional< IEquippedItem > tickPutAway( IItem item, EnumHand hand, EntityPlayer player ) {
		return this.wrapped.tickPutAway( item, hand, player );
	}
	
	@Override
	@SideOnly( Side.CLIENT )
	public void prepareRenderInHand( IItem item, EnumHand hand ) {
		this.wrapped.prepareRenderInHand( item, hand );
	}
	
	@Override
	@SideOnly( Side.CLIENT )
	public boolean renderInHand( IItem item, EnumHand hand ) {
		return this.wrapped.renderInHand( item, hand );
	}
	
	@Override
	@SideOnly( Side.CLIENT )
	public boolean renderSpecificInHand( IItem item, EnumHand hand ) {
		return this.wrapped.renderSpecificInHand( item, hand );
	}
	
	@Override
	@SideOnly( Side.CLIENT )
	public boolean onMouseWheelInput( IItem item, int dwheel ) {
		return this.wrapped.onMouseWheelInput( item, dwheel );
	}
	
	@Override
	@SideOnly( Side.CLIENT )
	public abstract IEquippedItem onInputUpdate( IItem item, String name, IInput input );
	
	@Override
	@SideOnly( Side.CLIENT )
	public boolean shouldDisableCrosshair( IItem item ) {
		return this.wrapped.shouldDisableCrosshair( item );
	}
	
	@Override
	@SideOnly( Side.CLIENT )
	public boolean getViewBobbing( IItem item, boolean original ) {
		return this.wrapped.getViewBobbing( item, original );
	}
	
	@Override
	@SideOnly( Side.CLIENT )
	public float getMouseSensitivity( IItem item, float original_sensitivity ) {
		return this.wrapped.getMouseSensitivity( item, original_sensitivity );
	}
}
