package com.fmum.common.module;

import com.fmum.common.FMUM;
import com.fmum.common.type.TypePaintable;
import com.fmum.common.type.TypeTextParser.LocalTypeFileParser;

import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagList;

public abstract class TypeModular extends TypePaintable
{
	public static final LocalTypeFileParser<TypeModular>
		parser = new LocalTypeFileParser<>(TypePaintable.parser);
	static
	{
		// Slots and default installed modules TODO
		parser.addKeyword("Slots", (s, t) -> t.slots = Slot.parse(s, 1));
	}
	
	public Slot[] slots = Slot.DEF_SLOTS;
	
	protected TypeModular(String name) { super(name); }
	
	/**
	 * @param states States of this module
	 * @param stepLen Step length of the slot rail of the super module
	 * @return Install position shift
	 */
	public float getOffset(int[] states, float stepLen) { return 0F; }
	
	public boolean stream(NBTTagList tag, ModuleVisitor visitor)
	{
		// Visit this module
		if(visitor.visit(tag, this)) return true;
		
		// Visit each module installed on this module
		NBTTagList slotTag, moduleTag;
		for(int i = this.slots.length; --i >= 0; )
			for(int j = (slotTag = (NBTTagList)tag.get(i + 1)).tagCount(); --j >= 0; )
				if(
					TagModular.getType(
						moduleTag = (NBTTagList)slotTag.get(j)
					).stream(moduleTag, visitor)
				) return true;
		
		return false;
	}
	
	public boolean stream(
		NBTTagList tag,
		float x, float y, float z,
		float rotX,
		ModulePosRotVisitor visitor
	) {
		// Prepare sin and cos value
		float sin = rotX * FMUM.TO_RADIANS;
		float cos = (float)Math.cos(sin);
		sin = (float)Math.sin(sin);
		
		// Visit this module
		if(visitor.visit(tag, this, x, y, z, sin, cos, rotX)) return true;
		
		// Go through each slot
		for(int i = this.slots.length; --i >= 0; )
		{
			Slot slot = this.slots[i];
			float ax = x + slot.x;
			float ay = y + slot.y * cos - slot.z * sin;
			float az = z + slot.y * sin + slot.z * cos;
			float arotX = rotX + slot.rotX;
			
			// Go through each module installed on this slot
			NBTTagList slotTag = (NBTTagList)tag.get(i + 1);
			for(int j = slotTag.tagCount(); --j >= 0; )
			{
				NBTTagList moduleTag = (NBTTagList)slotTag.get(j);
				int[] states = TagModular.getStates(moduleTag);
				TypeModular type = TagModular.getType(states);
				if(
					type.stream(
						moduleTag,
						ax + type.getOffset(states, slot.stepLen), ay, az,
						arotX,
						visitor
					)
				) return true;
			}
		}
		
		return false;
	}
	
	public final int getStreamIndex(NBTTagList tag, byte[] location, int len, int cursor)
	{
		if(cursor == len) return 0;
		
		int index = 1;
		int tarSlot = location[cursor];
		int tarModule = location[cursor + 1];
		
		NBTTagList slotTag, moduleTag;
		for(int i = this.slots.length; --i >= 0; )
			for(
				int j = (
					slotTag = (NBTTagList)tag.get(i + 1)
				).tagCount();
				--j >= 0;
			) {
				TypeModular type = TagModular.getType(
					moduleTag = (NBTTagList)slotTag.get(j)
				);
				if(i == tarSlot && j == tarModule)
					return index + type.getStreamIndex(moduleTag, location, len, cursor + 2);
				index += type.count(moduleTag);
			}
		
		// Should never reach here
		return -1;
	}
	
	@Override
	public void postParse()
	{
		super.postParse();
		
		// Do not forget to apply the model scale
		this.scale(this.modelScale);
	}
	
	/**
	 * @param tag Module base tag
	 * @return How many modules installed on this module including itself
	 */
	protected final int count(NBTTagList tag)
	{
		int count = 1;
		
		NBTTagList slotTag, moduleTag;
		for(int i = this.slots.length; --i >= 0; )
			for(
				int j = (
					slotTag = (NBTTagList)tag.get(i + 1)
				).tagCount();
				--j >= 0;
				count += TagModular.getType(moduleTag = (NBTTagList)slotTag.get(j)).count(moduleTag)
			);
		
		return count;
	}
	
	protected void scale(float s) { for(Slot slot : this.slots) slot.scale(s); }
	
	@FunctionalInterface
	public static interface ModuleVisitor {
		public boolean visit(NBTTagList tag, TypeModular type);
	}
	
	@FunctionalInterface
	public static interface ModulePosRotVisitor
	{
		public boolean visit(
			NBTTagList tag,
			TypeModular type,
			float x, float y, float z,
			float sin, float cos,
			float rotX
		);
	}
	
	public static class TagModular
	{
		public static final String TAG = "0";
		
		public static final byte
			ID_DAM = 0,
			FLAG = 1;
		
		/**
		 * TODO:
		 */
		public static final int MAX_DAMAGE = 128;
		
		/**
		 * Get states from given compound tag
		 * 
		 * @param tag Tag to retrieve states
		 * @return States in form of integer array
		 */
		public static int[] getStates(NBTTagList tag) {
			return ((NBTTagList)tag.get(0)).getIntArrayAt(0);
		}
		
//		public static boolean getFlag(int[] states, int mask) { return (states[FLAG] & mask) != 0; }
		
		public static int getDam(int[] states) { return states[ID_DAM] << 16 >>> 16; }
		
		public static void setIdDam(int[] states, int id, int dam) {
			states[ID_DAM] = id << 16 | dam << 16 >>> 16;
		}
		
//		public static int getDam(NBTTagList tag) { return getStates(tag)[ID_DAM] << 16 >>> 16; }
		
//		public static int getId(int[] states) { return states[ID_DAM] >>> 16; }
		
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
	}
}
