package com.fmum.client.input;

import com.fmum.common.FMUM;
import com.fmum.common.meta.IMeta;
import com.fmum.common.meta.Registry;

import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * @see InputHandler
 * @author Giant_Salted_Fish
 */
@SideOnly( Side.CLIENT )
public interface IKeyBind extends IMeta, IInput
{
	Registry< IKeyBind > REGISTRY = new Registry<>();
	
	/**
	 * Called by {@link InputHandler} on input event to update the state of this key bind.
	 */
	void update( boolean down );
	
	/**
	 * Called by {@link InputHandler} on input event if its update group is inactive.
	 */
	void inactiveUpdate( boolean down );
	
	/**
	 * Called when enters controls GUI to set key code back to corresponding {@link KeyBinding}.
	 * 
	 * @see #clearMcKeyBind()
	 */
	void restoreMcKeyBind();
	
	/**
	 * Called when quits controls GUI to clear key code of corresponding {@link KeyBinding}. In this
	 * way {@link FMUM} could have keys that bind to the same key on keyboard without have conflict.
	 * 
	 * @see #restoreMcKeyBind()
	 * @return
	 *     {@code true} if the key bind has been change. This is important as {@link InputHandler}
	 *     would save the key binds to local disk if any key bind has changed.
	 */
	boolean clearMcKeyBind();
	
	/**
	 * @return Key code that bounden to this key.
	 */
	int keyCode();
	
	/**
	 * Called by {@link InputHandler} to restore key bind since last time the game is closed.
	 * 
	 * @param code Key code to set.
	 */
	void setKeyCode( int code );
}
