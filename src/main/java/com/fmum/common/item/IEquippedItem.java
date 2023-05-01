package com.fmum.common.item;

import com.fmum.client.input.IInput;
import com.fmum.client.input.Key;
import com.fmum.client.player.PlayerPatchClient;
import com.fmum.client.render.IAnimator;

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
	static final IEquippedItem< ? > VANILLA = new IEquippedItem< IItem >()
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
		public IAnimator animator() { return IAnimator.NONE; }
	};
	
	T item();
	
	/**
	 * <p> Called when this item is holden in player's hand. </p>
	 * 
	 * <p> {@link EntityPlayer} and {@link EnumHand} is supplied here because we know outer caller
	 * should have a reference to them and this allows this class to eliminate the memory cost to
	 * refer to same instance. And this also helps to prevent others from calling this method as
	 * they do not have the corresponding context to supply. </p>
	 */
	default void tickInHand( EntityPlayer player, EnumHand hand ) { }
	
	default void handlePacket( ByteBuf buf, EntityPlayer player ) { }
	
	/**
	 * Called before camera update. Use this to update the {@link Animation} instance if it controls
	 * the camera animation.
	 * 
	 * @see #prepareRenderInHandSP(EnumHand)
	 */
	@SideOnly( Side.CLIENT )
	default void updateAnimationForRender( EnumHand hand ) { }
	
	/**
	 * Called before actual render of first person in hand, and after
	 * {@link #updateAnimationForRender(EnumHand)}. You can apply camera control and other setup
	 * works here to prepare the actual render.
	 * 
	 * @see #renderInHandSP(EnumHand)
	 */
	@SideOnly( Side.CLIENT )
	default void prepareRenderInHandSP( EnumHand hand ) { }
	
	/**
	 * @see PlayerPatchClient#onRenderHandSP()
	 * @param hand Actual hand to render in.
	 * @return {@code true} if should cancel original hand render.
	 */
	@SideOnly( Side.CLIENT )
	boolean renderInHandSP( EnumHand hand );
	
	/**
	 * @see PlayerPatchClient#onRenderSpecificHand(EnumHand)
	 * @return {@code true} if should cancel original hand render.
	 */
	@SideOnly( Side.CLIENT )
	boolean onRenderSpecificHandSP( EnumHand hand );
	
	@SideOnly( Side.CLIENT )
	default void renderInHand( EntityPlayer player, EnumHand hand ) { }
	
	/**
	 * This method is called when a key bind is triggered(pressed) when holding this item.
	 * 
	 * @see #onKeyRelease(IInput)
	 * @param key
	 *     Key bind being triggered. You can switch via its name with constants provided in
	 *     {@link Key}.
	 */
	@SideOnly( Side.CLIENT )
	default void onKeyPress( IInput key ) { }
	
	/**
	 * @see #onKeyPress(IInput)
	 */
	@SideOnly( Side.CLIENT )
	default void onKeyRelease( IInput key ) { }
	
	@SideOnly( Side.CLIENT )
	default boolean onMouseWheelInput( int dWheel ) { return false; }
	
	@SideOnly( Side.CLIENT )
	default boolean updateViewBobbing( boolean original ) { return original; }
	
	@SideOnly( Side.CLIENT )
	default boolean hideCrosshair() { return false; }
	
	@SideOnly( Side.CLIENT )
	IAnimator animator();
}
