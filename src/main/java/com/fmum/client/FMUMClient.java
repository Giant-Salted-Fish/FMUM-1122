package com.fmum.client;

import java.util.LinkedList;

import com.fmum.common.FMUM;
import com.fmum.common.util.Vec3f;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiControls;
import net.minecraft.client.gui.GuiOptions;
import net.minecraft.client.gui.GuiVideoSettings;
import net.minecraft.client.settings.GameSettings;
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
		public final float[] testFloat = { 0F, 0F, 0F, 0F, 0F, 0F };
		
		public void getPos(Vec3f dest) {
			dest.set(this.testFloat[0], this.testFloat[1], this.testFloat[2]);
		}
		
		public void getRot(Vec3f dest) {
			dest.set(this.testFloat[3], this.testFloat[4], this.testFloat[5]);
		}
	}
	/** for test */
	
	public static final int FMUM_CHAT_ID_BASE = 'F' + 'M' + 'U' + 'M';
	
	/**
	 * Easy referencing
	 */
	public static final Minecraft mc = FMUM.mc;
	public static final GameSettings settings = mc.gameSettings;
	// TODO: packet handler
	
	public static float
		oriFOV = settings.fovSetting,
		oriGamma = settings.gammaSetting,
		oriMouseSensi = settings.mouseSensitivity;
	public static int oriThirdPerson = settings.thirdPersonView;
	public static boolean oriViewBobbing = settings.viewBobbing;
	
	/**
	 * Game GUI in last tick
	 */
	public static Gui prevGUI = null;
	
	/**
	 * Whether manual mode is on
	 */
	public static boolean manualMode = false;
	
	private FMUMClient() { }
	
	static void tick()
	{
		// Abandon if have not entered a world yet
		EntityPlayerSP player = mc.player;
		if(player == null) return;
		
		/** for test */
		float[] f = testList.get(testInsNum).testFloat;
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
				f[testNum] += (tu ? 1F : -1F) * (testNum < 3 ? manualMode ? 0.1F : 0.5F : manualMode ? 1F : 5F);
			else if(te)
			{
				addChatMsg("cur ins: " + getTestInsString(testInsNum) + ", list size: " + testList.size(), 3);
				addChatMsg("pos xyz: " + f[0] + " " + f[1] + " " + f[2], 4);
				addChatMsg("rot xyz: " + f[3] + " " + f[4] + " " + f[5], 5);
//				RenderGun.createSmokeForGun = true;
			}
			else if(tq)
			{
				addChatMsg("set " + getTestString(testNum) + " to 0F", 2);
				f[testNum] = 0F;
			}
		}
		
		tl = tr = tu = td = te = tq = false;
		/** for test */
		
		// Check in game GUI change
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
			
			// Update previous GUI
			prevGUI = mc.currentScreen;
		}
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
