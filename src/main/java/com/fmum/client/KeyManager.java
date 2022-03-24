package com.fmum.client;

import java.util.LinkedList;
import java.util.List;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import com.fmum.common.FMUM;

import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * A static class that manages all the keys of {@link FMUM}
 * 
 * @author Giant_Salted_Fish
 */
@SideOnly(Side.CLIENT)
public abstract class KeyManager
{
	/**
	 * Call this on initialization to trigger lazy load of the keys
	 */
	public static void init() { Key.values(); }
	
	/**
	 * Key categories
	 */
	public static final String
		KEY_CATEGORY_FMUM = "key.category.fmum",
		KEY_CATEGORY_GUN = "key.category.fmum.gun",
		KEY_CATEGORY_ASSIST = "key.category.fmum.assist",
		KEY_CATEGORY_MODIFY = "key.category.fmum.modify",
		KEY_CATEGORY_TEST = "key.category.fmum.test";
	
	/**
	 * Keys that will update always update
	 */
	public static final LinkedList<Key> primaryKeys = new LinkedList<>();
	
	/**
	 * Keys that will update when {@link Key#CO} is pressed
	 */
	public static final LinkedList<Key> coKeys = new LinkedList<>();
	
	/**
	 * Keys that will update when {@link Key#CO} is not pressed
	 */
	public static final LinkedList<Key> inCoKeys = new LinkedList<>();
	
	public enum Key
	{
		/**
		 * <p>These keys will always update.</p>
		 * 
		 * <p>CATEGORY: {@link KeyManager#KEY_CATEGORY_TEST},
		 * {@link KeyManager#KEY_CATEGORY_MODIFY}.</p>
		 */
		TEST_UP("key.fmum.testup", Keyboard.KEY_UP, KEY_CATEGORY_TEST)
		{
			@Override
			protected void trigger() { FMUMClient.tu = true; }
		},
		TEST_DOWN("key.fmum.testdown", Keyboard.KEY_DOWN, KEY_CATEGORY_TEST)
		{
			@Override
			protected void trigger() { FMUMClient.td = true; }
		},
		TEST_LEFT("key.fmum.testleft", Keyboard.KEY_LEFT, KEY_CATEGORY_TEST)
		{
			@Override
			protected void trigger() { FMUMClient.tl = true; }
		},
		TEST_RIGHT("key.fmum.testright", Keyboard.KEY_RIGHT, KEY_CATEGORY_TEST)
		{
			@Override
			protected void trigger() { FMUMClient.tr = true; }
		},
		TEST_ENTER("key.fmum.testenter", Keyboard.KEY_NUMPAD5, KEY_CATEGORY_TEST)
		{
			@Override
			protected void trigger() { FMUMClient.te = true; }
		},
		TEST_QUIT("key.fmum.testquit", Keyboard.KEY_NUMPAD2, KEY_CATEGORY_TEST)
		{
			@Override
			protected void trigger() { FMUMClient.tq = true; }
		},
		
		
		/**
		 * <p>These keys will update if {@link #CO} is down. {@link #CO} itself is a special case.
		 * It will always update.</p>
		 * 
		 * <p>CATEGORY: {@link KeyManager#KEY_CATEGORY_ASSIST}.</p>
		 */
		CO("key.fmum.co", Keyboard.KEY_Z, KEY_CATEGORY_ASSIST, primaryKeys);
		
		public final KeyBinding key;
		public int keyCode;
		public int pressTime = 0;
		
		private Key(String name, int key, String category) { this(name, key, category, null); }
		
		private Key(String name, int key, String category, List<Key> group)
		{
			this.keyCode = key;
			ClientRegistry.registerKeyBinding(
				this.key = new KeyBinding(name, key, category)
			);
			
			// Group specified, add it to the group
			if(group != null)
				group.add(this);
			
			// Some special keys may required not to be update via list
			else switch(this.name())
			{
			case "FIRE":
			case "AIM_HOLD":
			case "AIM_TOGGLE":
			case "HOLD_BREATH":
			case "CO":
				return;
			default:
				switch(category)
				{
				case KEY_CATEGORY_TEST:
				case KEY_CATEGORY_MODIFY:
					primaryKeys.add(this);
					break;
				case KEY_CATEGORY_ASSIST:
					coKeys.add(this);
					break;
				case KEY_CATEGORY_FMUM:
				case KEY_CATEGORY_GUN:
					inCoKeys.add(this);
					break;
				default:
					// This should never happen
					throw new RuntimeException(
						"Unexpected key category <" + category
						+ "> from <" + this.name() + ">"
					);
				}
			}
		}
		
		public boolean down()
		{
			return(
				this.keyCode != Keyboard.KEY_NONE
				&& (
					this.keyCode < 0
					? Mouse.isButtonDown(this.keyCode + 100) 
					: Keyboard.isKeyDown(this.keyCode)
				)
			);
		}
		
		public boolean lastDown() { return this.pressTime > 1; }
		
		public boolean bounden() { return this.keyCode != Keyboard.KEY_NONE; }
		
		protected void update()
		{
			if(!this.down()) this.pressTime = 0;
			else if(this.pressTime++ == 0) this.trigger();
		}
		
		protected void trigger() { }
	}
	
	public static boolean keyDown(int keyCode)
	{
		return(
			keyCode != 0
			&& (
				keyCode < 0
				? Mouse.isButtonDown(keyCode + 100)
				: Keyboard.isKeyDown(keyCode)
			)
		);
	}
}
