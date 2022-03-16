package com.fmum.common.module;

import com.fmum.common.FMUM;
import com.fmum.common.type.TypePaintable;
import com.fmum.common.type.TypeTextParser.LocalTypeFileParser;

import net.minecraft.nbt.NBTTagList;

public abstract class TypeModule extends TypePaintable
{
	protected static final Slot[] DEF_SLOTS = { };
	
	public static final LocalTypeFileParser<TypeModule>
		parser = new LocalTypeFileParser<>(TypePaintable.parser);
	static
	{
		
		// Attachable slots and default attachments TODO
		parser.addKeyword("Slots", (s, t) -> t.slots = Slot.parse(s, 1));
		
	}
	
	/**
	 * Slots that this modifiable gun/attachment has for attaching attachments
	 */
	public Slot[] slots = DEF_SLOTS;
	
	protected TypeModule(String name, String contentPackName) { super(name, contentPackName); }
	
	/**
	 * Visit every modifiable installed on this modifiable. Note that this modifiable itself will
	 * also be visit. It stops visiting once visitor returns true.
	 * 
	 * @param visitor Visitor to visit each modifiable
	 * @param tag Base tag of current modifiable
	 * @param x Current x-coordinate
	 * @param y Current y-coordinate
	 * @param z Current z-coordinate
	 * @param rotX Current rotation along x-axis
	 * @return True if visitor returns true
	 */
	public final boolean stream(
		Visitor visitor,
		NBTTagList tag,
		float x, float y, float z,
		float rotX
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
			float az = z + slot.y * sin + slot.z + cos;
			float arotX = rotX + slot.rotX;
			
			// Go through each module installed on this slot
			NBTTagList slotTag = ((NBTTagList)tag.get(i + 1));
			for(int j = slotTag.tagCount(); --j >= 0; )
			{
				NBTTagList atag = (NBTTagList)slotTag.get(j);
				int[] states = TagModule.getStates(atag);
				TypeModule atype = 
			}
		}
	}
}
