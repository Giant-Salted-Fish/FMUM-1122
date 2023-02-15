package com.mcwb.client.input;

import com.mcwb.common.MCWB;
import com.mcwb.common.meta.IMeta;
import com.mcwb.common.meta.Registry;

import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly( Side.CLIENT )
public interface IKeyBind extends IMeta
{
	public static final Registry< IKeyBind > REGISTRY = new Registry<>();
	
	public String category();
	
	/**
	 * Called by {@link InputHandler} on input event to update the state of this key bind
	 */
	public void update( boolean down );
	
	/**
	 * Called by {@link InputHandler} on input event if its update group is blocked
	 */
	public void reset();
	
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
	 * @return Whether this key is currently being pressed
	 */
	public boolean down();
	
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
	 * <p> Usually to trigger the function of this key bind. If your key only trigger once the key
	 * is pressed then you can simply override this method to setup its effect. </p>
	 * 
	 * <p> Notice that you should not assume that calling this method will always trigger the effect
	 * of the key bind as it is possible to implement its functionality in {@link #update(boolean)}
	 * method although it is the recommended practice. </p>
	 * 
	 * TODO: really need this?
	 */
//	public default void fire() {
//		
//	}
}
