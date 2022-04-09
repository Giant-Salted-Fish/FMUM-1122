package com.fmum.client;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import javax.annotation.Nullable;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import com.fmum.common.FMUM;

import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * A static class that manages all the keys of {@link FMUM}. Key bindings will be set to
 * {@link Keyboard#KEY_NONE} in gaming to avoid key binding conflict. They will be set back during
 * GUI launched.
 * 
 * @author Giant_Salted_Fish
 */
@SideOnly(Side.CLIENT)
public abstract class KeyManager
{
	/**
	 * Key categories
	 */
	public static final String
		KEY_CATEGORY_FMUM = "keycategory.fmum",
		KEY_CATEGORY_GUN = "keycategory.fmum.gun",
		KEY_CATEGORY_ASSIST = "keycategory.fmum.assist",
		KEY_CATEGORY_MODIFY = "keycategory.fmum.modification",
		KEY_CATEGORY_TEST = "keycategory.fmum.test";
	
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
	
	/** TODO: validate necessary?
	 * Keys that will never update. They will be referenced independently.
	 */
//	public static final LinkedList<Key> independentKeys = new LinkedList<>();
	
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
			protected void fire() { FMUMClient.tu = true; }
		},
		TEST_DOWN("key.fmum.testdown", Keyboard.KEY_DOWN, KEY_CATEGORY_TEST)
		{
			@Override
			protected void fire() { FMUMClient.td = true; }
		},
		TEST_LEFT("key.fmum.testleft", Keyboard.KEY_LEFT, KEY_CATEGORY_TEST)
		{
			@Override
			protected void fire() { FMUMClient.tl = true; }
		},
		TEST_RIGHT("key.fmum.testright", Keyboard.KEY_RIGHT, KEY_CATEGORY_TEST)
		{
			@Override
			protected void fire() { FMUMClient.tr = true; }
		},
		TEST_ENTER("key.fmum.testenter", Keyboard.KEY_NUMPAD5, KEY_CATEGORY_TEST)
		{
			@Override
			protected void fire() { FMUMClient.te = true; }
		},
		TEST_QUIT("key.fmum.testquit", Keyboard.KEY_NUMPAD2, KEY_CATEGORY_TEST)
		{
			@Override
			protected void fire() { FMUMClient.tq = true; }
		},
		
		/**
		 * <p>These keys will update if {@link #CO} is not down.</p>
		 * 
		 * <p>CATEGORY: {@link KeyManager#KEY_CATEGORY_GUN}.</p>
		 */
		PULL_TRIGGER("key.fmum.pulltrigger", -100, KEY_CATEGORY_GUN, primaryKeys),
		AIM_HOLD("key.fmum.aimhold", -99, KEY_CATEGORY_GUN, primaryKeys),
		AIM_TOGGLE("key.fmum.aimtoggle", Keyboard.KEY_NONE, KEY_CATEGORY_GUN, primaryKeys),
		TOGGLE_MANUAL("key.fmum.togglemanual", Keyboard.KEY_PERIOD, KEY_CATEGORY_GUN)
		{
			@Override
			protected void fire()
			{
				super.fire();
				
				FMUMClient.addPromptMsg(
					I18n.format(
						(FMUMClient.manualMode = !FMUMClient.manualMode)
						? "msg.fmum.manualmodeon"
						: "msg.fmum.manualmodeoff"
					)
				);
			}
		},
		LOOK_AROUND("key.fmum.lookaround", Keyboard.KEY_LMENU, KEY_CATEGORY_GUN),
		VIEW_WEAPON("key.fmum.viewweapon", Keyboard.KEY_V, KEY_CATEGORY_GUN),
		TOGGLE_MODIFY("key.fmum.togglemodify", Keyboard.KEY_I, KEY_CATEGORY_GUN),
		
		/**
		 * <p>These keys will update if {@link #CO} is down. {@link #CO} itself is a special case.
		 * It will always update.</p>
		 * 
		 * <p>CATEGORY: {@link KeyManager#KEY_CATEGORY_ASSIST}.</p>
		 */
		CO("key.fmum.co", Keyboard.KEY_Z, KEY_CATEGORY_ASSIST, primaryKeys),
		CO_TOGGLE_MANUAL("key.fmum.cotogglemanual", Keyboard.KEY_NONE, KEY_CATEGORY_ASSIST)
		{
			@Override
			protected void fire() { TOGGLE_MANUAL.fire(); }
		},
		CO_LOOK_AROUND("key.fmum.colookaround", Keyboard.KEY_NONE, KEY_CATEGORY_ASSIST),
		CO_VIEW_WEAPON("key.fmum.coviewweapon", Keyboard.KEY_NONE, KEY_CATEGORY_ASSIST),
		CO_TOGGLE_MODIFY("key.fmum.cotogglemodify", Keyboard.KEY_NONE, KEY_CATEGORY_ASSIST),
		
		/**
		 * <p>These keys will always update.</p>
		 * 
		 * <p>CATEGORY: {@link KeyManager#KEY_CATEGORY_MODIFY}.</p>
		 */
		SELECT_UP("key.fmum.selectup", Keyboard.KEY_UP, KEY_CATEGORY_MODIFY),
		SELECT_DOWN("key.fmum.selectdown", Keyboard.KEY_DOWN, KEY_CATEGORY_MODIFY),
		SELECT_LEFT("key.fmum.selectleft", Keyboard.KEY_LEFT, KEY_CATEGORY_MODIFY),
		SELECT_RIGHT("key.fmum.selectright", Keyboard.KEY_RIGHT, KEY_CATEGORY_MODIFY),
		SELECT_CONFIRM("key.fmum.selectconfirm", Keyboard.KEY_G, KEY_CATEGORY_MODIFY),
		SELECT_CANCEL("key.fmum.selectcancel", Keyboard.KEY_H, KEY_CATEGORY_MODIFY),
		SELECT_TOGGLE("key.fmum.selecttoggle", Keyboard.KEY_V, KEY_CATEGORY_MODIFY);
		
		public final KeyBinding keyBind;
		public int keyCode;
		public boolean down = false;
		
		private Key(String name, int key, String category) { this(name, key, category, null); }
		
		private Key(String name, int key, String category, List<Key> group)
		{
			this(name, key, category, new KeyBinding(name, Keyboard.KEY_NONE, category), group);
			
			ClientRegistry.registerKeyBinding(this.keyBind);
		}
		
		private Key(
			String name,
			int key,
			String category,
			@Nullable KeyBinding keyBind,
			List<Key> group
		) {
			this.keyCode = key;
			this.keyBind = keyBind;
			
			// Group specified, add it to the group
			if(group != null)
				group.add(this);
			
			// Otherwise, assign group based on its category
			else switch(category)
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
		
//		public boolean lastDown() { return this.pressTime > 1; }
		
		public boolean bounden() { return this.keyCode != Keyboard.KEY_NONE; }
		
		protected void update()
		{
			if(!keyDown(this.keyCode)) this.down = false;
			else if(!this.down)
			{
				this.down = true;
				this.fire();
			}
		}
		
		protected void fire() { FMUMClient.prevItem.keyNotify(this); }
		
		public static boolean lookAroundActivated() {
			return (CO.down ? CO_LOOK_AROUND : LOOK_AROUND).down;
		}
	}
	
	public static void enterGUIControls() {
		for(Key k : Key.values()) if(k.keyBind != null) k.keyBind.setKeyCode(k.keyCode);
	}
	
	public static void quitGUIControls()
	{
		boolean needSyncOptions = false;
		for(Key k : Key.values())
		{
			if(k.keyCode != k.keyBind.getKeyCode())
			{
				k.keyCode = k.keyBind.getKeyCode();
				needSyncOptions = true;
			}
			k.keyBind.setKeyCode(Keyboard.KEY_NONE);
		}
		KeyBinding.resetKeyBindingArrayAndHash();
		
		if(needSyncOptions) saveTo(ClientProxy.keyBindsFile);
	}
	
	public static void saveTo(File file)
	{
		try(BufferedWriter out = new BufferedWriter(new FileWriter(file)))
		{
			for(Key k : Key.values())
			{
				out.write(k.name() + ":" + k.keyCode);
				out.newLine();
			}
		}
		catch(IOException e) { FMUM.log.error(I18n.format("fmum.errorsavingkeybinds"), e); }
	}
	
	public static void readFrom(File file)
	{
		try(BufferedReader in = new BufferedReader(new FileReader(file)))
		{
			for(String l; (l = in.readLine()) != null; )
			{
				final int i = l.indexOf(':');
				try
				{
					Enum.valueOf(
						Key.class,
						l.substring(0, i)
					).keyCode = Integer.parseInt(l.substring(i + 1));
				}
				catch(NumberFormatException e) {
					FMUM.log.error(I18n.format("fmum.keycodeformaterror", l));
				}
				catch(IllegalArgumentException e) {
					FMUM.log.error(I18n.format("fmum.unrecognizedkeybind", l));
				}
			}
		}
		catch(IOException e) { FMUM.log.error(I18n.format("fmum.errorreadingkeybinds"), e); }
	}
	
	public static boolean keyDown(int keyCode)
	{
		return(
			keyCode != Keyboard.KEY_NONE
			&& (
				keyCode < 0
				? Mouse.isButtonDown(keyCode + 100)
				: Keyboard.isKeyDown(keyCode)
			)
		);
	}
}
