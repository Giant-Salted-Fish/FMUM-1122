package com.fmum.common.module;

import java.util.HashMap;

import com.fmum.common.type.TypePaintable;
import com.fmum.common.type.TypeTextParser.LocalTypeFileParser;
import com.fmum.common.util.CoordSystem;

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
	
	@Override
	public void postParse()
	{
		super.postParse();
		
		// Do not forget to apply the model scale
		this.scale(this.modelScale);
		
		modules.put(this.name, this);
	}
	
	/**
	 * @param states States of this module
	 * @param stepLen Step length of the slot rail of the super module
	 * @return Install position shift
	 */
	public double getPos(int[] states, double stepLen) { return 0D; }
	
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
		double x, double y, double z,
		double rotX,
		ModulePosRotVisitor visitor
	) {
		// Prepare sin and cos value
		double sin = Math.toRadians(rotX);
		double cos = Math.cos(sin);
		sin = Math.sin(sin);
		
		// Visit this module
		if(visitor.visit(tag, this, x, y, z, sin, cos, rotX)) return true;
		
		// Go through each slot
		for(int i = this.slots.length; --i >= 0; )
		{
			Slot slot = this.slots[i];
			double ax = x + slot.x;
			double ay = y + slot.y * cos - slot.z * sin;
			double az = z + slot.y * sin + slot.z * cos;
			double arotX = rotX + slot.rotX;
			
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
						ax + type.getPos(states, slot.stepLen), ay, az,
						arotX,
						visitor
					)
				) return true;
			}
		}
		
		return false;
	}

	/**
	 * Stream for renderer. Difference from a normal stream is that the visitor is capable to change
	 * the position of current module and it will effect all the modules installed on this module.
	 * 
	 * @param tag Base tag of current module
	 * @param thisSys Coordinate system that this module is positioned in
	 * @param visitor Visitor to visit each module
	 */
	public final void stream(NBTTagList tag, CoordSystem thisSys, RenderInfoVisitor visitor)
	{
		// Visit this module
		visitor.visit(tag, this, thisSys);
		
		// Check if this module has slots to iterate
		if(this.slots.length == 0) return;
		
		// Fetch coordinate systems
		final CoordSystem moduleSys = CoordSystem.pool.poll();
		final CoordSystem slotSys = CoordSystem.pool.poll();
		
		// Go through each slot
		for(int i = this.slots.length; --i >= 0; )
		{
			// Check if there exists some installed modules
			NBTTagList slotTag = (NBTTagList)tag.get(i + 1);
			if(slotTag.tagCount() == 0) continue;
			
			// Setup coordinate system
			final Slot slot = this.slots[i];
			slotSys.set(thisSys);
			slotSys.trans(slot);
			if(slot.rotX != 0D)
				slotSys.rot(slot.rotX, CoordSystem.X);
			slotSys.submitRot();
			
			for(int j = slotTag.tagCount(); --j >= 0; )
			{
				NBTTagList moduleTag = (NBTTagList)slotTag.get(j);
				int[] states = TagModular.getStates(moduleTag);
				TypeModular moduleType = TagModular.getType(states);
				
				moduleSys.set(slotSys);
				moduleSys.trans(moduleType.getPos(states, slot.stepLen), CoordSystem.NORM_X);
				moduleType.stream(moduleTag, moduleSys, visitor);
			}
		}
		
		CoordSystem.pool.back(slotSys);
		CoordSystem.pool.back(moduleSys);
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
	
	/**
	 * Generate tag for a modular item stack
	 * 
	 * @param dam Damage of the item
	 * @return Generated tag that can set in item's nbt tag
	 */
	public NBTTagList genTag(int dam)
	{
		NBTTagList tag = this.genTag(dam, 0, 0);
		
		// Add default modules
		this.defaultModules.writeToTag(tag);
		
		return tag;
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
		
		// Append slot tag
		for(int i = this.slots.length; --i >= 0; tag.appendTag(new NBTTagList()));
		return tag;
	}
	
	public final String getTexture(NBTTagList tag) {
		return this.paintjobs.get(TagModular.getDam(tag)).texture;
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
	
	protected void scale(double s) { for(Slot slot : this.slots) slot.scale(s); }
	
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
			double x, double y, double z,
			double sin, double cos,
			double rotX
		);
	}
	
	@FunctionalInterface
	public static interface RenderInfoVisitor
	{
		/**
		 * See {@link TypeModular#stream(RenderInfoVisitor, NBTTagList, CoordSystem)}
		 * 
		 * @param tag Tag of this module
		 * @param type Type of this module
		 * @param sys
		 *     Position of this module. Note that this is just a buffer and it is likely to be
		 *     changed in the future. If you want to keep it please fetch a {@link CoordSystem}
		 *     instance and copy the value of this system to it. Changing position of this system
		 *     will have effect on all attachments installed on it.
		 */
		public void visit(NBTTagList tag, TypeModular type, CoordSystem sys);
	}
}
