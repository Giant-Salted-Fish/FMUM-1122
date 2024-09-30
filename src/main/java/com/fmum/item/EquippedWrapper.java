package com.fmum.item;

import com.fmum.input.IInput;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumHand;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public abstract class EquippedWrapper implements IEquippedItem
{
	public final IEquippedItem wrapped;
	
	protected EquippedWrapper( IEquippedItem wrapped ) {
		this.wrapped = wrapped;
	}
	
	@Override
	public abstract IEquippedItem tickInHand( EnumHand hand, IItem item, EntityPlayer player );
	
	@Override
	@SideOnly( Side.CLIENT )
	public void prepareRenderInHand( EnumHand hand, IItem item ) {
		this.wrapped.prepareRenderInHand( hand, item );
	}
	
	@Override
	@SideOnly( Side.CLIENT )
	public boolean renderInHand( EnumHand hand, IItem item ) {
		return this.wrapped.renderInHand( hand, item );
	}
	
	@Override
	@SideOnly( Side.CLIENT )
	public boolean renderSpecificInHand( EnumHand hand, IItem item ) {
		return this.wrapped.renderSpecificInHand( hand, item );
	}
	
	@Override
	@SideOnly( Side.CLIENT )
	public boolean onMouseWheelInput( int dwheel, IItem item ) {
		return this.wrapped.onMouseWheelInput( dwheel, item );
	}
	
	@Override
	@SideOnly( Side.CLIENT )
	public abstract IEquippedItem onInputUpdate( String name, IInput input, IItem item );
	
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
	public float getMouseSensitivity( float original_sensitivity, IItem item ) {
		return this.wrapped.getMouseSensitivity( original_sensitivity, item );
	}
}
