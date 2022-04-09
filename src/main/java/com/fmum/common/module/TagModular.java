package com.fmum.common.module;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

/**
 * <p>methods to handle module states</p>
 * 
 * <pre>
 *       14       2      10       6
 * 00000000000000 00 0000000000 000000
 *       |        |      |        |
 *       |      offset   |       flag
 *   undefined          step
 * </pre>
 */
public abstract class TagModular
{
	public static final String TAG = "0";
	
	public static final byte NUM_STATES = 2;
	
	public static final byte
		ID_DAM = 0,
		FLAG = 1;
	
	/**
	 * TODO:
	 */
	public static final int MAX_DAMAGE = 128;
	
	/**
	 * Check whether given module stack has valid module tag
	 * 
	 * @param stack Stack to check
	 * @return {@code true} if given stack has valid tag
	 */
	public static boolean validateTag(ItemStack stack)
	{
		NBTTagCompound tag = stack.getTagCompound();
		return tag != null && tag.hasKey(TAG);
	}
	
	public static void setupTag(ItemStack stack)
	{
		NBTTagCompound compound = stack.getTagCompound();
		if(compound == null)
			stack.setTagCompound(
				compound = new NBTTagCompound()
			);
		
		compound.setTag(
			TAG,
			((ItemModular)stack.getItem()).getType().genTag(stack.getItemDamage())
		);
	}
	
	public static NBTTagList getTag(ItemStack stack) {
		return (NBTTagList)stack.getTagCompound().getTag(TAG);
	}
	
	/**
	 * Get states from given compound tag
	 * 
	 * @param tag Tag to retrieve states
	 * @return States in form of integer array
	 */
	public static int[] getStates(NBTTagList tag) {
		return ((NBTTagList)tag.get(0)).getIntArrayAt(0);
	}
	
	public static int getDam(NBTTagList tag) { return getStates(tag)[ID_DAM] << 16 >>> 16; }
	
	public static int getDam(int[] states) { return states[ID_DAM] << 16 >>> 16; }
	
	public static void setDam(int[] states, int dam) {
		states[ID_DAM] = states[ID_DAM] & 0xFFFF0000 | dam & 0xFFFF;
	}
	
	public static void setIdDam(int[] states, int id, int dam) {
		states[ID_DAM] = id << 16 | dam & 0xFFFF;
	}
	
	public static float getState(int[] states, int i) {
		return Float.intBitsToFloat(states[i]);
	}
	
	public static void setState(int[] states, float value, int i) {
		states[i] = Float.floatToIntBits(value);
	}
	
	public static TypeModular getType(int[] states) {
		return ((ItemModular)Item.getItemById(states[ID_DAM] >>> 16)).getType();
	}
	
	public static TypeModular getType(NBTTagList tag)
	{
		return ((ItemModular)Item.getItemById(
			((NBTTagList)tag.get(0)).getIntArrayAt(0)[ID_DAM] >>> 16
		)).getType();
	}
	
	public static int getStep(int[] states) { return states[FLAG] << 16 >>> 22; }
	
	private static final int STEP_MASK = -1 << 6 & -1 >>> 16;
	/**
	 * Step has 10 valid bit(ranging 0-1023). See {@link TagModular}.
	 */
	public static void setStep(int[] states, int step) {
		states[FLAG] = states[FLAG] & ~STEP_MASK | step << 6 & STEP_MASK;
	}
	
	public static int getOffset(int[] states) { return states[FLAG] << 14 >>> 30; }
	
	private static final int OFFSET_MASK = -1 << 16 & -1 >>> 14;
	/**
	 * Offset has 2 valid bit(ranging 0-3). See {@link TagModular}. 
	 */
	public static void setOffset(int[] states, int offset) {
		states[FLAG] = states[FLAG] & ~OFFSET_MASK | offset << 16 & OFFSET_MASK;
	}
}
