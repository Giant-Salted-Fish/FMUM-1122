package com.fmum.common.module;

import java.util.HashMap;

import com.fmum.common.FMUM;
import com.fmum.common.type.TypePaintable;
import com.fmum.common.type.TypeTextParser.LocalTypeFileParser;

import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagIntArray;
import net.minecraft.nbt.NBTTagList;

public abstract class TypeModular extends TypePaintable
{
	public static final HashMap<String, TypeModular> modules = new HashMap<>();
	
	public static final LocalTypeFileParser<TypeModular>
		parser = new LocalTypeFileParser<>(TypePaintable.parser);
	static
	{
		parser.addKeyword("Slots", (s, t) -> t.slots = Slot.parse(s, 1));
		parser.addKeyword(
			"DefaultModules",
			(s, t) -> (t.defaultModules = new DefaultModules(t.name)).parse(s, 1)
		);
	}
	
	protected static final DefaultModules DEF_DEF_MODULES = new DefaultModules(null);
	
	public Slot[] slots = Slot.DEF_SLOTS;
	
	public DefaultModules defaultModules = DEF_DEF_MODULES;
	
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
		
		modules.put(this.name, this);
	}
	
	public NBTTagList genTag(int dam, int step, int offset)
	{
		// Create tag and states tag
		NBTTagList tag = new NBTTagList();
		NBTTagList statesWrapper = new NBTTagList();
		int[] states = this.genStates();
		statesWrapper.appendTag(new NBTTagIntArray(states));
		tag.appendTag(statesWrapper);
		
		// Write default states
		TagModular.setIdDam(states, Item.getIdFromItem(this.item), dam);
		TagModular.setStep(states, step);
		TagModular.setOffset(states, offset);
		
		// Add default modules
		this.defaultModules.writeToTag(tag);
		
		// Append slot tag
		for(int i = this.slots.length; --i >= 0; tag.appendTag(new NBTTagList()));
		return tag;
	}
	
	/**
	 * Generate {@code int} array to set as states of this module. Sub-types can override this
	 * method to have more customized states.
	 * 
	 * @return Raw states array
	 */
	protected int[] genStates() { return new int[TagModular.NUM_STATES]; }
	
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
}
