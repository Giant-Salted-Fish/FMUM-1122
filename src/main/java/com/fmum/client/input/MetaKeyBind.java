package com.fmum.client.input;

import java.util.Map;
import java.util.TreeMap;

import com.fmum.common.FMUM;
import com.fmum.common.meta.EnumMeta;
import com.fmum.common.meta.MetaGrouped;

import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly( Side.CLIENT )
public interface MetaKeyBind extends MetaGrouped
{
	public static final TreeMap< String, MetaKeyBind > regis = new TreeMap<>();
	
	@Override
	public default void regisPostInitHandler( Map< String, Runnable > tasks )
	{
		MetaGrouped.super.regisPostInitHandler( tasks );
		
		tasks.put( "REGIS_KEY_BIND", () -> this.regisTo( this, regis ) );
	}
	
	@Override
	default void regisPostLoadHandler( Map< String, Runnable > tasks ) {
		MetaGrouped.super.regisPostLoadHandler( tasks );
	}
	
	/**
	 * @return Key code that bounden to this key
	 */
	public int keyCode();
	
	/**
	 * Called by {@link InputHandler} to restore key bind since last time the game is closed
	 * 
	 * @param code Key code to set
	 */
	public void $keyCode( int code );
	
	/**
	 * @return Whether this key is currently being pressed
	 */
	public boolean down();
	
	public void $down( boolean down );
	
	/**
	 * Called when enters controls GUI to set key code back to corresponding {@link KeyBinding}.
	 * 
	 * @see #clearMCKeyBind()
	 */
	public void showMCKeyBind();
	
	/**
	 * Called when quits controls GUI to clear key code of corresponding {@link KeyBinding}. In this
	 * way {@link FMUM} could have keys that bind to the same key on keyboard without have conflict.
	 * 
	 * @see #showMCKeyBind()
	 * @return
	 *     {@code true} if the key bind has been change. This is important as {@link InputHandler}
	 *     would save the key binds to local disk if any key bind has changed.
	 */
	public boolean clearMCKeyBind();
	
	/**
	 * Called by {@link InputHandler} on input event to update the state of this key bind
	 */
	public default void update()
	{
		if( !InputHandler.down( this.keyCode() ) )
			this.$down( false );
		else if( !this.down() )
		{
			this.$down( true );
			this.fire();
		}
	}
	
	/**
	 * <p> Usually to trigger the function of this key bind. If your key only trigger once the key
	 * is pressed then you can simply override this method to setup its effect. </p>
	 * 
	 * <p> Notice that you should not assume that calling this method will always trigger the effect
	 * of the key bind as it is possible to implement its functionality in {@link #update()} method
	 * although it is the recommended practice. </p>
	 */
	public default void fire() { }
	
	@Override
	public default EnumMeta enumMeta() { return EnumMeta.KEY_BIND; }
}
