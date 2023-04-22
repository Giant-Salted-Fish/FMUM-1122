package com.mcwb.client.input;

import com.mcwb.common.MCWB;
import com.mcwb.common.meta.IMeta;
import com.mcwb.common.meta.Registry;

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
	public static final Registry< IKeyBind > REGISTRY = new Registry<>();
	
	public String category();
	
	/**
	 * Called by {@link InputHandler} on input event to update the state of this key bind.
	 */
	public void update( boolean down );
	
	/**
	 * Called by {@link InputHandler} on input event if its update group is inactive.
	 */
	public void inactiveUpdate( boolean down );
	
	/**
	 * Called when enters controls GUI to set key code back to corresponding {@link KeyBinding}.
	 * 
	 * @see #clearMcKeyBind()
	 */
	public void restoreMcKeyBind();
	
	/**
	 * Called when quits controls GUI to clear key code of corresponding {@link KeyBinding}. In this
	 * way {@link MCWB} could have keys that bind to the same key on keyboard without have conflict.
	 * 
	 * @see #restoreMcKeyBind()
	 * @return
	 *     {@code true} if the key bind has been change. This is important as {@link InputHandler}
	 *     would save the key binds to local disk if any key bind has changed.
	 */
	public boolean clearMcKeyBind();
	
	/**
	 * @return Key code that bounden to this key.
	 */
	public int keyCode();
	
	/**
	 * Called by {@link InputHandler} to restore key bind since last time the game is closed.
	 * 
	 * @param code Key code to set.
	 */
	public void setKeyCode( int code );
}
