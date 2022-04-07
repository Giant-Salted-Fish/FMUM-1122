package com.fmum.client;

import java.util.LinkedList;

import com.fmum.common.FMUM;
import com.fmum.common.network.PacketHandler;
import com.fmum.common.type.ItemInfo;
import com.fmum.common.util.Messager;
import com.fmum.common.util.Vec3;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiControls;
import net.minecraft.client.gui.GuiOptions;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiVideoSettings;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MouseHelper;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public abstract class FMUMClient
{
	/** for test */
	public static boolean tu = false, td = false, tl = false, tr = false, te = false, tq = false;
	
	public static int testNum = 0, testInsNum = 0;
	
	public static final LinkedList<TestPosRot> testList = new LinkedList<>();
	static
	{
		testList.add(new TestPosRot());
		testList.add(new TestPosRot());
		testList.add(new TestPosRot());
	}
	
	//public static HitBoxes hbs0 = null, hbs1 = null;
	public static EntityPlayerSP player;
	
	public static String getTestString(int num)
	{
		switch(num)
		{
		case 0: return "Translate - x";
		case 1: return "Translate - y";
		case 2: return "Translate - z";
		case 3: return "Rotate - x";
		case 4: return "Rotate - y";
		case 5: return "Rotate - z";
		default: return "undefined - " + (num - 6);
		}
	}
	
	public static String getTestInsString(int num)
	{
		switch(num)
		{
		case 0: return "Primary Pos Rot";
		case 1: return "Left Arm Pos Rot";
		case 2: return "Right Arm Pos Rot";
		default: return "Custom Pos Rot " + (num - 2);
		}
	}
	
	public static final class TestPosRot
	{
		public final double[] testValue = { 0D, 0D, 0D, 0D, 0D, 0D };
		
		public void getPos(Vec3 dst) {
			dst.set(this.testValue[0], this.testValue[1], this.testValue[2]);
		}
		
		public void getRot(Vec3 dst) {
			dst.set(this.testValue[3], this.testValue[4], this.testValue[5]);
		}
	}
	
	public static void toggleManualTell(Messager... msgs)
	{
		if(!manualMode) return;
		
		for(int i = 0; i < msgs.length; ++i)
			addChatMsg(msgs[i].message(), i);
		manualMode = false;
	}
	/** for test */
	
	public static final int FMUM_CHAT_ID_BASE = 'F' + 'M' + 'U' + 'M';
	
	/**
	 * Easy referencing
	 */
	public static final Minecraft mc = FMUM.mc;
	public static final GameSettings settings = mc.gameSettings;
	public static final PacketHandler netHandler = FMUM.netHandler;
	
	/**
	 * Client side ticker
	 */
	public static int ticker = 0;
	
	public static float
		oriFOV = settings.fovSetting,
		oriGamma = settings.gammaSetting,
		oriMouseSensi = settings.mouseSensitivity;
	public static int oriThirdPerson = settings.thirdPersonView;
	public static boolean oriViewBobbing = settings.viewBobbing;
	
	/**
	 * Game GUI in last tick
	 */
	public static GuiScreen prevGUI = null;
	
	/**
	 * Inventory slot selected last tick
	 */
	public static int prevSlot = 0;
	
	/**
	 * Item stack that holden last tick
	 */
	public static ItemStack prevStack = ItemStack.EMPTY;
	
	/**
	 * Item holden last tick
	 */
	public static ItemInfo prevItem = ItemInfo.NONE;
	
	/**
	 * Whether manual mode is on
	 */
	public static boolean manualMode = false;
	
	/**
	 * Operation that is executing. Instead of setting a new operation directly, it is recommended
	 * to call {@link #tryLaunchOp(Operation, ItemStack)} if you want to launch a new operation.
	 */
	public static Operation operating = Operation.NONE;
	
	protected static final MouseHelper MOUSE_HELPER_PROXY = new MouseHelper()
	{
		@Override
		public void mouseXYChange()
		{
			super.mouseXYChange();
			
			// Call render tick and do pre-render
			prevItem.renderTick(prevStack, this);
		}
	};
	
	private FMUMClient() { }
	
	static void tick()
	{
		++ticker;
		// TODO: check speed up
		
		// Abandon if have not entered a world yet
		EntityPlayerSP player = mc.player;
		if(player == null) return;
		
		/** for test */
		double[] d = testList.get(testInsNum).testValue;
		
		EventHandlerClient.renderCamRoll = (float)d[3];
		if(KeyManager.Key.CO.down())
		{
			if(tl);
			else if(tr);
			else if(tu || td)
				addChatMsg(
					"switch to " + getTestInsString(
						tu
						? testInsNum < 1 ? testInsNum = testList.size() - 1 : --testInsNum
						: testInsNum >= testList.size() - 1 ? testInsNum = 0 : ++testInsNum
					),
					2
				);
			else if(te)
			{
				addChatMsg("created: " + getTestInsString(testList.size()), 2);
				testList.add(new TestPosRot());
			}
			else if(tq)
			{
				if(testList.size() > 1)
				{
					testList.removeLast();
					addChatMsg("removed: " + getTestInsString(testList.size()));
					if(testInsNum >= testList.size())
						testInsNum = testList.size() - 1;
				}
				else addChatMsg("can not remove last one instance", 2);
			}
		}
		else
		{
			if(tl || tr)
				addChatMsg(
					"move to " + getTestString(
						tl
						? testNum < 1 ? testNum = 5 : --testNum
						: testNum > 4 ? testNum = 0 : ++testNum
					),
					2
				);
			else if(tu || td)
				d[testNum] += (tu ? 1D : -1D) * (testNum < 3 ? manualMode ? 0.1D : 0.5D : manualMode ? 1D : 5D);
			else if(te)
			{
				addChatMsg("cur ins: " + getTestInsString(testInsNum) + ", list size: " + testList.size(), 3);
				addChatMsg("pos xyz: " + d[0] + " " + d[1] + " " + d[2], 4);
				addChatMsg("rot xyz: " + d[3] + " " + d[4] + " " + d[5], 5);
//				RenderGun.createSmokeForGun = true;
			}
			else if(tq)
			{
				addChatMsg("set " + getTestString(testNum) + " to 0D", 2);
				d[testNum] = 0D;
			}
		}
		
		tl = tr = tu = td = te = tq = false;
		/** for test */
		
		ItemStack stack = player.inventory.getCurrentItem();
		ItemInfo item = (
			stack.getItem() instanceof ItemInfo
			? (ItemInfo)stack.getItem()
			: ItemInfo.NONE
		);
		
		if(player.inventory.currentItem != prevSlot)
		{
			settings.viewBobbing = oriViewBobbing && !item.disableViewBobbing();
			if(operating.switchItem(stack))
				operating = Operation.NONE;
			item.onTakeOut(stack);
			
			prevSlot = player.inventory.currentItem;
		}
		
		// Tick item and current operation TODO: figure out which one is better to be the first
		item.tick(stack);
		if(operating.tick(stack))
			operating = Operation.NONE;
		
		// Check in game GUI change
		// TODO: mods like Optfine may add more layers in settings
		if(mc.currentScreen != prevGUI)
		{
			// TODO: operation
			
			// Show key binds if control GUI is activated
			if(mc.currentScreen instanceof GuiControls)
			{
				KeyManager.enterGUIControls();
				settings.mouseSensitivity = oriMouseSensi;
			}
			else if(prevGUI instanceof GuiControls)
			{
				KeyManager.quitGUIControls();
				oriMouseSensi = settings.mouseSensitivity;
			}
			
			// Set back option values if options GUI is launched
			else if(mc.currentScreen instanceof GuiOptions)
			{
				settings.fovSetting = oriFOV;
				settings.viewBobbing = oriViewBobbing;
//				settings.gammaSetting = oriGamma; TODO: force gamma setting
			}
			else if(prevGUI instanceof GuiOptions || prevGUI instanceof GuiVideoSettings)
			{
				oriFOV = settings.fovSetting;
				oriViewBobbing = settings.viewBobbing;
//				oriGamma TODO: force gamma
				
				// If it is gun in hand, then set back settings for gun
				// TODO
			}
			
			// Update last tick GUI
			prevGUI = mc.currentScreen;
		}
		
		// Player can still change current hold stack by replacing the item in current slot. Hence
		// it is needed to update it every tick.
		prevStack = stack;
		prevItem = item;
		
		// Ensure mouse helper(Mods like Flan's Mod may change mouse help in certain conditions)
		mc.mouseHelper = MOUSE_HELPER_PROXY;
	}
	
	/**
	 * Try to launch the given operation
	 * 
	 * @param op Operation to launch
	 * @return Whether succeed to launch the given operation
	 */
	public static boolean tryLaunchOp(Operation op)
	{
		if(!operating.encounter(op)) return false;
		
		(operating = op).launch(prevStack);
		return true;
	}
	
	public static void addChatMsg(String msg, int id)
	{
		mc.ingameGUI.getChatGUI().printChatMessageWithOptionalDeletion(
			new TextComponentString(msg),
			FMUM_CHAT_ID_BASE + id
		);
	}
	
	public static void addChatMsg(String msg) {
		mc.ingameGUI.getChatGUI().printChatMessage(new TextComponentString(msg));
	}
}
