package com.mcwb.common.item;

import com.mcwb.client.input.IKeyBind;
import com.mcwb.client.input.Key;
import com.mcwb.client.player.PlayerPatchClient;
import com.mcwb.client.render.IAnimator;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumHand;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Represents the {@link IItem} equipped in player's hand.
 * 
 * @author Giant_Salted_Fish
 */
public interface IEquippedItem< T extends IItem >
{
	public static final IEquippedItem< ? > VANILLA = new IEquippedItem< IItem >()
	{
		@Override
		public IItem item() { return IItem.VANILLA; }
		
		@Override
		@SideOnly( Side.CLIENT )
		public boolean renderInHandSP( EnumHand hand ) { return false; }
		
		@Override
		@SideOnly( Side.CLIENT )
		public boolean onRenderSpecificHandSP( EnumHand hand ) { return false; }
		
		@Override
		@SideOnly( Side.CLIENT )
		public IAnimator animator() { return IAnimator.INSTANCE; }
	};
	
	public T item();
	
	/**
	 * <p> Called when this item is holden in player's hand. </p>
	 * 
	 * <p> {@link EntityPlayer} and {@link EnumHand} is supplied here because we know outer caller
	 * should have a reference to them and this allows this class to eliminate the memory cost to
	 * refer to same instance. And this also helps to prevent others from calling this method as
	 * they do not have the corresponding context to supply. </p>
	 */
	public default void tickInHand( EntityPlayer player, EnumHand hand ) { }
	
	public default void handlePacket( ByteBuf buf, EntityPlayer player ) { }
	
//	public default void onPutAway( IEquippedItem oldItem, EntityPlayer player, EnumHand hand ) { }
//	
//	/**
//	 * Called when player is trying swap this main hand item to off-hand.
//	 * 
//	 * @return {@code true} to prevent this swap.
//	 */
//	public default boolean onSwapHand( EntityPlayer player ) { return false; }
	
	/**
	 * Called before actual render of first person in hand. You can apply camera control and other
	 * setup works here to prepare the actual render.
	 * 
	 * @see #renderInHandSP(EnumHand)
	 */
	@SideOnly( Side.CLIENT )
	public default void prepareRenderInHandSP( EnumHand hand ) { }
	
	/**
	 * @see PlayerPatchClient#onRenderHandSP()
	 * @param hand Actual hand to render in.
	 * @return {@code true} if should cancel original hand render.
	 */
	@SideOnly( Side.CLIENT )
	public boolean renderInHandSP( EnumHand hand );
	
	/**
	 * @see PlayerPatchClient#onRenderSpecificHand(EnumHand)
	 * @return {@code true} if should cancel original hand render.
	 */
	@SideOnly( Side.CLIENT )
	public boolean onRenderSpecificHandSP( EnumHand hand );
	
	@SideOnly( Side.CLIENT )
	public default void renderInHand( EntityPlayer player, EnumHand hand ) { }
	
	/**
	 * This method is called when a key bind is triggered(pressed) when holding this item.
	 * 
	 * @see #onKeyRelease(IKeyBind)
	 * @param key
	 *     Key bind being triggered. You can switch via its name with constants provided in
	 *     {@link Key}.
	 */
	@SideOnly( Side.CLIENT )
	public default void onKeyPress( IKeyBind key ) { }
	
	/**
	 * @see #onKeyPress(IKeyBind)
	 */
	@SideOnly( Side.CLIENT )
	public default void onKeyRelease( IKeyBind key ) { }
	
	@SideOnly( Side.CLIENT )
	public default boolean onMouseWheelInput( int dWheel ) { return false; }
	
	@SideOnly( Side.CLIENT )
	public default boolean updateViewBobbing( boolean original ) { return original; }
	
	@SideOnly( Side.CLIENT )
	public default boolean hideCrosshair() { return false; }
	
	@SideOnly( Side.CLIENT )
	public IAnimator animator();
}
