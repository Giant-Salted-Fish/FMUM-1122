package com.fmum.client;

import java.util.LinkedList;

import com.fmum.common.FMUM;
import com.fmum.common.network.PacketHandler;
import com.fmum.common.type.ItemInfo;
import com.fmum.common.util.Vec3f;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiControls;
import net.minecraft.client.gui.GuiOptions;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiVideoSettings;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.item.ItemStack;
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
	public static final PacketHandler netHandler = FMUM.netHandler;
	
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
	 * Whether manual mode is on
	 */
	public static boolean manualMode = false;
	
	/**
	 * Operation that is executing. Instead of setting a new operation directly, it is recommended
	 * to call {@link #tryLaunchOp(Operation, ItemStack)} if you want to launch a new operation.
	 */
	public static Operation operating = Operation.NONE;
	
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
		
		ItemStack stack = player.inventory.getCurrentItem();
		if(stack.getItem() instanceof ItemInfo)
		{
			ItemInfo item = (ItemInfo)stack.getItem();
			
			// Notify take out if just switched to this item
			if(player.inventory.currentItem != prevSlot)
			{
				settings.viewBobbing = oriViewBobbing && !item.shouldDisableViewBobbing();
				if(operating.switchItem(stack))
					operating = Operation.NONE;
				item.onTakeOut(stack);
			}
			
			if(item.tagReady(stack))
			{
			}
		}
		else
		{
			if(player.inventory.currentItem != prevSlot)
			{
				settings.viewBobbing = oriViewBobbing;
				if(operating.switchItem(ItemStack.EMPTY))
					operating = Operation.NONE;
			}
			// TODO: default model to control camera effects
		}
		
		
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
			
			// Update previous GUI
			prevGUI = mc.currentScreen;
		}
	}
	
	/**
	 * Try to launch the given operation
	 * 
	 * @param op Operation to launch
	 * @param stack Holding stack
	 * @return Whether succeed to launch the given operation
	 */
	public static boolean tryLaunchOp(Operation op, ItemStack stack)
	{
		if(!operating.encounter(op)) return false;
		
		(operating = op).launch(stack);
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
	
	public static class Operation
	{
		public static final Operation NONE = new Operation();
		
		public float getSmoothedProgress(float smoother) { return 1F; }
		
		/**
		 * Tick this operation
		 * 
		 * @param stack Current holding stack
		 * @return {@code true} if this operation has complete and should exit
		 */
		protected boolean tick(ItemStack stack) { return false; }
		
		/**
		 * Launch this operation with given holding stack
		 * 
		 * @param stack ItemStack that the player is currently holding
		 */
		protected void launch(ItemStack stack) { }
		
		/**
		 * A new operation is arriving, check if this operation volunteer to terminate itself so the
		 * new operation can execute immediately
		 * 
		 * @param op New operation
		 * @return {@code true} if this operation abandon its execution and let new operation to run
		 */
		protected boolean encounter(Operation op) { return true; }
		
		/**
		 * Called when player switch to a new holding item
		 * 
		 * @param stack New holding item
		 * @return {@code true} if should give up execution of this operation
		 */
		protected boolean switchItem(ItemStack stack) { return true; }
		
		/**
		 * @return {@code true} if should kill this operation on GUI change
		 */
		protected boolean onGUIChange(GuiScreen gui) { return true; }
	}
	
	/**
	 * Super type of operations that usually have a fixed amount of execution time
	 * 
	 * @author Giant_Salted_Fish
	 */
	public static abstract class ProgressiveOperation extends Operation
	{
		public float progress = 0F;
		public float prevProgress = 0F;
		
		public float progressor = 1F / 16F;
		
		@Override
		public final float getSmoothedProgress(float smoother) {
			return this.prevProgress + (this.progress - this.prevProgress) * smoother;
		}
		
		@Override
		protected void launch(ItemStack stack) { this.progress = this.prevProgress = 0F; }
		
		@Override
		protected boolean tick(ItemStack stack)
		{
			this.prevProgress = this.progress;
			if((this.progress += this.progressor) > 1F)
				this.progress = 1F;
			return this.prevProgress >= 1F;
		}
	}
}
