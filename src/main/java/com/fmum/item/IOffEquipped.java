package com.fmum.item;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Optional;

public interface IOffEquipped
{
	default IOffEquipped tickInHand( IItem item, EntityPlayer player ) {
		return this;
	}
	
	default Optional< IOffEquipped > tickPutAway( IItem item, EntityPlayer player ) {
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
	default boolean renderInHand( IItem item ) {
		return false;
	}
	
	@SideOnly( Side.CLIENT )
	default boolean renderSpecificInHand( IItem item ) {
		return false;
	}
}
