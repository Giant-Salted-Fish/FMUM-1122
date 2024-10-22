package com.fmum.item;

import com.fmum.input.IInput;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Optional;

public interface IMainEquipped
{
	/**
	 * Animation channel for item held in hand.
	 */
	String CHANNEL_ITEM = "item";
	
	String CHANNEL_LEFT_ARM = "left_arm";
	String CHANNEL_RIGHT_ARM = "right_arm";
	
	
	default IMainEquipped tickInHand( IItem item, EntityPlayer player ) {
		return this;
	}
	
	default Optional< IMainEquipped > tickPutAway( IItem item, EntityPlayer player ) {
		return Optional.empty();
	}
	
	@SideOnly( Side.CLIENT )
	default void prepareRenderInHand( IItem item ) {
		// Override this method to prepare your customized first person in hand render.
	}
	
	/**
	 * @return
	 *     Return {@code true} if you want to handle this yourself and cancel
	 *     vanilla hand render.
	 * @see net.minecraftforge.client.event.RenderHandEvent
	 */
	@SideOnly( Side.CLIENT )
	boolean renderInHand( IItem item );
	
	/**
	 * @return
	 *      Return {@code true} if you want to handle this yourself and cancel
	 *      vanilla specific hand render.
	 * @see net.minecraftforge.client.event.RenderSpecificHandEvent
	 */
	@SideOnly( Side.CLIENT )
	boolean renderSpecificInHand( IItem item );
	
	/**
	 * @return Whether to cancel corresponding mouse event.
	 */
	@SideOnly( Side.CLIENT )
	default boolean onMouseWheelInput( int dwheel, IItem item ) {
		return false;
	}
	
	@SideOnly( Side.CLIENT )
	default IMainEquipped onInputUpdate( String name, IInput input, IItem item ) {
		return this;
	}
	
	@SideOnly( Side.CLIENT )
	default boolean shouldDisableCrosshair( IItem item ) {
		return false;
	}
	
	/**
	 * @param original User's original setting for view bobbing.
	 * @return Enable view bobbing or not.
	 */
	@SideOnly( Side.CLIENT )
	default boolean getViewBobbing( boolean original, IItem item ) {
		return original;
	}
	
	@SideOnly( Side.CLIENT )
	default float getMouseSensitivity( float ori_sensi, IItem item ) {
		return ori_sensi;
	}
}
