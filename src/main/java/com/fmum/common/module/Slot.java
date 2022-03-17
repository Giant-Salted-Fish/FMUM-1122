package com.fmum.common.module;

import java.util.LinkedList;
import java.util.TreeSet;

import com.fmum.common.FMUM;

/**
 * @author Giant_Salted_Fish
 */
public final class Slot
{
	/**
	 * A fix instance that can be used as the initializer value
	 */
	public static final Slot[] DEF_SLOTS = { };
	
	/**
	 * Relative position of the tail of this slot
	 */
	public float
		x = 0F,
		y = 0F,
		z = 0F;
	
	/**
	 * Orientation of this slot. Its the rotation along x-axis.
	 */
	public float rotX = 0F;
	
	/**
	 * How far it goes for each adjustment step
	 */
	public float stepLen = 0F;
	
	/**
	 * Max steps that the attachments can go on this slot
	 */
	public short maxStep = 0;
	
	/**
	 * Maximum number of attachments can be attached to this slot
	 */
	public byte maxCanAttach = 1;
	
	/**
	 * White list and blacklist of the type of attachments
	 */
	public TreeSet<String>
		categoryWhitelist = FMUM.EMPTY_STR_SET,
		categoryBlacklist = FMUM.EMPTY_STR_SET;
	
	/**
	 * White list and blacklist of the attachments
	 */
	public TreeSet<String>
		attachmentWhitelist = FMUM.EMPTY_STR_SET,
		attachmentBlacklist = FMUM.EMPTY_STR_SET;
	
	public void scale(float s)
	{
		this.x *= s;
		this.y *= s;
		this.z *= s;
		this.stepLen *= s;
	}
	
	public boolean isAllowed(TypeModular type)
	{
		return(
			this.attachmentWhitelist.contains(type.name)
			|| (
				!this.attachmentBlacklist.contains(type.name)
				&& (
					this.categoryWhitelist.contains(type.category)
					|| !this.categoryBlacklist.contains(type.category)
				)
			)
		);
	}
	
	private static final LinkedList<Slot> slots = new LinkedList<>();
	synchronized public static Slot[] parse(String[] split, int cursor)
	{
		// A error may occurred in last parse and leaving elements in buffer list.
		// Clear it before we start to use it
		slots.clear();
		
		Slot slot = new Slot();
		for(int i = cursor; i < split.length; ++i)
			switch(split[i])
			{
			case "|":
				slots.add(slot);
				slot = new Slot();
				cursor = i + 1;
				break;
			
			case "A[":
				int after = parseList(split, "]", i, slot.attachmentWhitelist = new TreeSet<>());
				cursor += after - i + 1;
				i = after;
				break;
			
			case "A<":
				after = parseList(split, ">", i, slot.attachmentBlacklist = new TreeSet<>());
				cursor += after - i + 1;
				i = after;
				break;
			
			case "C[":
				after = parseList(split, "]", i, slot.categoryWhitelist = new TreeSet<>());
				cursor += after - i + 1;
				i = after;
				break;
			
			case "C<":
				after = parseList(split, ">", i, slot.categoryBlacklist = new TreeSet<>());
				cursor += after - i + 1;
				i = after;
				break;
			
			default:
				switch(i - cursor)
				{
				case 0:
					slot.x = Float.parseFloat(split[i]) / 16F;
					break;
				case 1:
					slot.y = Float.parseFloat(split[i]) / 16F;
					break;
				case 2:
					slot.z = Float.parseFloat(split[i]) / 16F;
					break;
				case 3:
					slot.rotX = Float.parseFloat(split[i]);
					break;
				case 4:
					slot.stepLen = Float.parseFloat(split[i]) / 16F;
					break;
				case 5:
					slot.maxStep = Short.parseShort(split[i]);
					break;
				case 6:
					slot.maxCanAttach = Byte.parseByte(split[i]);
					break;
				default:
					throw new RuntimeException("Too many arguments for one attachable slot");
				}
			}
		
		if(slot != null) slots.add(slot);
		Slot[] ret = slots.toArray(new Slot[slots.size()]);
		slots.clear();
		return ret;
	}
	
	private static int parseList(String[] split, String close, int cursor, TreeSet<String> dest)
	{
		while(!split[++cursor].equals(close))
			dest.add(split[cursor]);
		return cursor;
	}
}
