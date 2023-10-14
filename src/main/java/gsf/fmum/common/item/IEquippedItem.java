package gsf.fmum.common.item;

import gsf.fmum.client.input.IInput;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumHand;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public interface IEquippedItem< T extends IItem >
{
	IEquippedItem< ? > VANILLA = new IEquippedItem< IItem >()
	{
		@Override
		public IItem item() { return IItem.VANILLA; }
		
		@Override
		public boolean onRenderHand( EnumHand hand )
		{
			final boolean cancel_vanilla_hand_render = false;
			return cancel_vanilla_hand_render;
		}
		
		@Override
		public boolean onRenderSpecificHand( EnumHand hand )
		{
			final boolean cancel_vanilla_hand_render = false;
			return cancel_vanilla_hand_render;
		}
	};
	
	T item();
	
	default void tickInHand( IItem item, EntityPlayer player, EnumHand hand ) { }
	
	// TODO: Add support for off-hand?
	default void onItemPacket( ByteBuf buf, EntityPlayer player ) { }
	
	default void prepareRenderInHand( EnumHand enumHand ) { }
	
	/**
	 * @see net.minecraftforge.client.event.RenderHandEvent
	 * @return {@code true} if should cancel vanilla hand render.
	 */
	@SideOnly( Side.CLIENT )
	boolean onRenderHand( EnumHand hand );
	
	@SideOnly( Side.CLIENT )
	boolean onRenderSpecificHand( EnumHand hand );
	
	@SideOnly( Side.CLIENT )
	default boolean onMouseWheelInput( int dwheel )
	{
		final boolean should_cancel_event = false;
		return should_cancel_event;
	}
	
	@SideOnly( Side.CLIENT )
	default void onInputSignal( String signal, IInput input ) { }
	
	@SideOnly( Side.CLIENT )
	default boolean updateViewBobbing( boolean original ) {
		return original;
	}
	
	@SideOnly( Side.CLIENT )
	default boolean shouldHideCrosshair() {
		return false;
	}
	
	@SideOnly( Side.CLIENT )
	default float getMouseSensitivity( float original ) {
		return original;
	}
}
