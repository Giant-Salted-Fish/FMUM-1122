package com.fmum.common.module;

import java.util.LinkedList;
import java.util.TreeSet;

import com.fmum.common.type.TypeInfo;
import com.fmum.common.util.Vec3;

/**
 * @author Giant_Salted_Fish
 */
public final class Slot extends Vec3
{
	/**
	 * A fix instance that can be used as the initializer value
	 */
	public static final Slot[] DEF_SLOTS = { };
	
	/**
	 * Orientation of this slot. Its the rotation along x-axis.
	 */
	public double rotX = 0D;
	
	/**
	 * How far it goes for each adjustment step
	 */
	public double stepLen = 0D;
	
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
		categoryWhitelist = TypeInfo.EMPTY_STR_SET,
		categoryBlacklist = TypeInfo.EMPTY_STR_SET;
	
	/**
	 * White list and blacklist of the attachments
	 */
	public TreeSet<String>
		attachmentWhitelist = TypeInfo.EMPTY_STR_SET,
		attachmentBlacklist = TypeInfo.EMPTY_STR_SET;
	
	public Slot scale(double s)
	{
		super.scale(s);
		this.stepLen *= s;
		return this;
	}
	
	public boolean isAllowed(TypeModular type)
	{
		return(
			this.attachmentWhitelist.size() > 0
			? this.attachmentWhitelist.contains(type.name)
			: (
				!this.attachmentBlacklist.contains(type.name)
				&& (
					this.categoryWhitelist.size() > 0
					? this.categoryWhitelist.contains(type.category)
					: !this.categoryBlacklist.contains(type.category)
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
					slot.x = Double.parseDouble(split[i]) / 16D;
					break;
				case 1:
					slot.y = Double.parseDouble(split[i]) / 16D;
					break;
				case 2:
					slot.z = Double.parseDouble(split[i]) / 16D;
					break;
				case 3:
					slot.rotX = Double.parseDouble(split[i]);
					break;
				case 4:
					slot.stepLen = Double.parseDouble(split[i]) / 16D;
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
