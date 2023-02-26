package com.mcwb.common.item;

import com.mcwb.client.input.IKeyBind;
import com.mcwb.client.input.Key;
import com.mcwb.client.player.PlayerPatchClient;
import com.mcwb.common.meta.IContexted;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * <p> Represents {@link IItemType} with context. </p>
 * 
 * <p> WARNING: You should not assume that the contexted will stay the same if type does not change
 * due to some special reason. Especially true for the modules. Hence re-fetch and update if the
 * contexted you are using is buffered from somewhere before. </p>
 * 
 * @see IItemType
 * @author Giant_Salted_Fish
 */
public interface IItem extends IContexted
{
	/**
	 * For empty stack. Cancel item render if is place in off-hand.
	 */
	public static final IItem EMPTY = new IItem()
	{
		@Override
		@SideOnly( Side.CLIENT )
		public boolean renderInHand( EnumHand hand ) { return hand == EnumHand.OFF_HAND; }
		
		@Override
		@SideOnly( Side.CLIENT )
		public boolean onRenderSpecificHand( EnumHand hand ) { return hand == EnumHand.OFF_HAND; }
		
		@Override
		@SideOnly( Side.CLIENT )
		public ResourceLocation texture() { return null; }
	};
	
	/**
	 * For vanilla item that is not empty. They will be rendered in original ways.
	 */
	public static final IItem VANILLA = new IItem()
	{
		@Override
		@SideOnly( Side.CLIENT )
		public boolean renderInHand( EnumHand hand ) { return false; }
		
		@Override
		@SideOnly( Side.CLIENT )
		public boolean onRenderSpecificHand( EnumHand hand ) { return false; }
		
		@Override
		@SideOnly( Side.CLIENT )
		public ResourceLocation texture() { return null; }
	};
	
	/**
	 * A default use context
	 */
	public static final IUseContext USE_CONTEXT = new IUseContext()
	{
		private final NBTTagCompound data = new NBTTagCompound();
		
		@Override
		public NBTTagCompound data() { return this.data; }
	};
	
	/**
	 * Called when this item is holden in player's hand
	 */
	public default void tickInHand( EntityPlayer player, EnumHand hand ) { }
	
	/**
	 * Called when player trying to switch to another main hand item
	 */
//	public default void onPutAway( IItem newItem, EntityPlayer player ) { }
	
	public default IUseContext onTakeOut( IItem oldItem, EntityPlayer player, EnumHand hand ) {
		return USE_CONTEXT;
	}
	
	public default IUseContext onInHandStackChanged(
		IItem oldItem,
		EntityPlayer player,
		EnumHand hand
	) { return USE_CONTEXT; }
	
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
	
	@SideOnly( Side.CLIENT )
	public ResourceLocation texture(); // TODO: check if this is used
	
	/**
	 * TODO: proper intro
	 * 
	 * @author Giant_Salted_Fish
	 */
	@FunctionalInterface
	public static interface IUseContext { public NBTTagCompound data(); }
}
