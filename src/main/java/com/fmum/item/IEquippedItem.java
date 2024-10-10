package com.fmum.item;

import com.fmum.input.IInput;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumHand;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Optional;

public interface IEquippedItem
{
	/**
	 * Animation channel for item held in hand.
	 */
	String CHANNEL_ITEM = "item";
	
	String CHANNEL_LEFT_ARM = "left_arm";
	String CHANNEL_RIGHT_ARM = "right_arm";
	
	
	default IEquippedItem tickInHand( IItem item, EnumHand hand, EntityPlayer player ) {
		return this;
	}
	
	default Optional< IEquippedItem > tickPutAway( IItem item, EnumHand hand, EntityPlayer player ) {
		return Optional.empty();
	}
	
	@SideOnly( Side.CLIENT )
	default void prepareRenderInHand( IItem item, EnumHand hand ) {
		// Override this method to prepare your customized first person in hand render.
	}
	
	/**
	 * @return
	 *     Return {@code true} if you want to handle this yourself and cancel
	 *     vanilla hand render.
	 * @see net.minecraftforge.client.event.RenderHandEvent
	 */
	@SideOnly( Side.CLIENT )
	boolean renderInHand( IItem item, EnumHand hand );
	
	/**
	 * @return
	 *      Return {@code true} if you want to handle this yourself and cancel
	 *      vanilla specific hand render.
	 * @see net.minecraftforge.client.event.RenderSpecificHandEvent
	 */
	@SideOnly( Side.CLIENT )
	boolean renderSpecificInHand( IItem item, EnumHand hand );
	
	/**
	 * @return Whether to cancel corresponding mouse event.
	 */
	@SideOnly( Side.CLIENT )
	default boolean onMouseWheelInput( IItem item, int dwheel ) {
		return false;
	}
	
	@SideOnly( Side.CLIENT )
	default IEquippedItem onInputUpdate( IItem item, String name, IInput input ) {
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
	default boolean getViewBobbing( IItem item, boolean original ) {
		return original;
	}
	
	@SideOnly( Side.CLIENT )
	default float getMouseSensitivity( IItem item, float original_sensitivity ) {
		return original_sensitivity;
	}
}
