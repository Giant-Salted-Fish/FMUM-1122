package com.mcwb.common.item;

import com.mcwb.client.input.IKeyBind;
import com.mcwb.client.input.Key;
import com.mcwb.client.player.PlayerPatchClient;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.util.EnumHand;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Represents the {@link Item} in player's hand
 * 
 * @author Giant_Salted_Fish
 */
public interface IEquippedItem
{
	/**
	 * For empty stack. Cancel item render if is place in off-hand.
	 */
	public static final IEquippedItem EMPTY = new IEquippedItem()
	{
		@Override
		public IItem item() { return IItem.EMPTY; }
		
		@Override
		@SideOnly( Side.CLIENT )
		public boolean renderInHand( EnumHand hand ) { return hand == EnumHand.OFF_HAND; }
		
		@Override
		@SideOnly( Side.CLIENT )
		public boolean onRenderSpecificHand( EnumHand hand ) { return hand == EnumHand.OFF_HAND; }
	};
	
	/**
	 * For vanilla item that is not empty. They will be rendered in original ways.
	 */
	public static final IEquippedItem VANILLA = new IEquippedItem()
	{
		@Override
		public IItem item() { return IItem.VANILLA; }
		
		@Override
		@SideOnly( Side.CLIENT )
		public boolean renderInHand( EnumHand hand ) { return false; }
		
		@Override
		@SideOnly( Side.CLIENT )
		public boolean onRenderSpecificHand( EnumHand hand ) { return false; }
	};
	
	public IItem item();
	
	/**
	 * Called when this item is holden in player's hand
	 */
	public default void tickInHand( EntityPlayer player, EnumHand hand ) { }
	
	/**
	 * Called when this item is holden in player's hand
	 */
//	public default void onPutAway(
//		NIItem< ? > oldItem,
//		EntityPlayer player, EnumHand hand
//	) { }
	
	/**
	 * Called when player is trying swap this main hand item to off-hand
	 * 
	 * @return {@code true} to prevent this swap
	 */
	public default boolean onSwapHand( EntityPlayer player ) { return false; }
	
	/**
	 * <p> It is not the appropriate time to render your models. Instead it gives you a chance to
	 * prepare something before you do the actual rendering. </p>
	 * 
	 * <p> You better apply camera control here if it is required as the camera setup is right after
	 * this method call. </p>
	 */
	@SideOnly( Side.CLIENT )
	public default void prepareRenderInHand( EnumHand hand ) { }
	
	/**
	 * @see PlayerPatchClient#onRenderHand()
	 * @param hand Actual hand to render in
	 * @return {@code true} if should cancel original hand render
	 */
	@SideOnly( Side.CLIENT )
	public boolean renderInHand( EnumHand hand );
	
	/**
	 * @see PlayerPatchClient#onRenderSpecificHand(EnumHand)
	 * @param hand Actual hand
	 * @return {@code true} if should cancel original hand render
	 */
	@SideOnly( Side.CLIENT )
	public boolean onRenderSpecificHand( EnumHand hand );
	
	/**
	 * This method is called when a key bind is triggered(pressed) when holding this item
	 * 
	 * @param key
	 *     Key bind being triggered. You can switch via its name with constants provided in
	 *     {@link Key}.
	 */
	@SideOnly( Side.CLIENT )
	public default void onKeyPress( IKeyBind key ) { }
	
	@SideOnly( Side.CLIENT )
	public default void onKeyRelease( IKeyBind key ) { }
	
	@SideOnly( Side.CLIENT )
	public default boolean onMouseWheelInput( int dWheel ) { return false; }
	
	@SideOnly( Side.CLIENT )
	public default boolean updateViewBobbing( boolean original ) { return original; }
	
	@SideOnly( Side.CLIENT )
	public default boolean hideCrosshair() { return false; }
}
