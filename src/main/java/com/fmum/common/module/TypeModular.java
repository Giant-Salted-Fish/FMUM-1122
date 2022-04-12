package com.fmum.common.module;

import java.util.HashMap;

import javax.annotation.Nullable;

import com.fmum.common.CommonProxy;
import com.fmum.common.paintjob.TypePaintable;
import com.fmum.common.type.TypeTextParser.LocalTypeFileParser;
import com.fmum.common.util.CoordSystem;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagIntArray;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ResourceLocation;

public abstract class TypeModular extends TypePaintable
{
	public static final HashMap<String, TypeModular> modules = new HashMap<>();
	
	public static final LocalTypeFileParser<TypeModular>
		parser = new LocalTypeFileParser<>(TypePaintable.parser);
	static
	{
		parser.addKeyword(
			"Offsets",
			(s, t) -> {
				t.offsets = new double[s.length - 1];
				for(int i = s.length; --i > 0; t.offsets[i - 1] = Double.parseDouble(s[i]));
			}
		);
		parser.addKeyword("Slots", (s, t) -> t.slots = Slot.parse(s, 1));
		parser.addKeyword(
			"DefaultModules",
			(s, t) -> (t.defaultModules = new DefaultModules(t.name)).parse(s, 1)
		);
		parser.addKeyword("BigHitbox", (s, t) -> t.parseHitbox(s, 0));
		parser.addKeyword("Hitbox", (s, t) -> t.parseHitbox(s, 1));
	}
	
	public static final Slot[] DEF_SLOTS = { };
	
	protected static final double[] DEF_OFFSETS = { 0D };
	
	protected static final DefaultModules DEF_DEF_MODULES = new DefaultModules(null);
	
	protected static final Hitboxes DEF_HITBOXES = new Hitboxes();
	
	public Slot[] slots = DEF_SLOTS;
	
	public double[] offsets = DEF_OFFSETS;
	
	public DefaultModules defaultModules = DEF_DEF_MODULES;
	
	/**
	 * <pre> 0 = big(raw) hit box
	 * 1 = delicate hit box</pre>
	 */
	public Hitboxes[] hitbox = { DEF_HITBOXES, DEF_HITBOXES };
	
	protected TypeModular(String name) { super(name); }
	
	@Override
	public void postParse()
	{
		super.postParse();
		
		// Do not forget to apply the model scale
		for(Slot slot : this.slots)
			slot.scale(this.scale);
		for(int i = this.offsets.length; i-- > 0; this.offsets[i] *= this.scale);
		
		int i = this.hitbox.length - 1;
		if(this.hitbox[i] != DEF_HITBOXES)
		{
			this.hitbox[i].scale(this.scale);
			
			while(i-- > 0)
				if(this.hitbox[i] == DEF_HITBOXES)
					this.hitbox[i] = this.hitbox[i + 1];
				else this.hitbox[i].scale(this.scale);
		}
		
		modules.put(this.name, this);
	}
	
	/**
	 * @param states States of this module
	 * @param stepLen Step length of the slot rail of the super module
	 * @return Install position shift
	 */
	public final double getPosX(int[] states, double stepLen) {
		return this.getPosX(TagModular.getStep(states), TagModular.getOffset(states), stepLen);
	}
	
	/**
	 * Used in modification mode where this module is still in preview mode and not yet been
	 * installed
	 * 
	 * @see #getPosX(int[], double)
	 */
	public double getPosX(int step, int offset, double stepLen) {
		return step * stepLen + this.offsets[offset];
	}
	
	/**
	 * @return {@code true} if number of module installed is below the maximum of the slot
	 */
	public final boolean availableForInstall(NBTTagList tag, int slot)
	{
		int numInstalled = ((NBTTagList)tag.get(1 + slot)).tagCount();
		return(
			numInstalled < this.slots[slot].maxCanInstall
			&& numInstalled < CommonProxy.maxCanInstall
		);
	}
	
	public boolean checkPreviewConflict(
		NBTTagList thisTag,
		CoordSystem installPos,
		ItemStack toInstall
	) {
		final HitboxTesters
			baseHitboxes = HitboxTesters.get(thisTag, this),
			installeeHitboxes = HitboxTesters.get(
				TagModular.getTag(toInstall),
				((ItemModular)toInstall.getItem()).getType(),
				installPos
			);
		
		boolean ret = baseHitboxes.conflictWith(installeeHitboxes);
		baseHitboxes.release();
		installeeHitboxes.release();
		return ret;
	}
	
	/**
	 * @param tag Tag of this module
	 * @return Number of nodes in the deepest path including this node 
	 */
	public final int getDeepestPathNodeCount(NBTTagList tag)
	{
		int max = 1;
		for(int i = this.slots.length; i-- > 0; )
		{
			NBTTagList slotTag = (NBTTagList)tag.get(1 + i);
			int j = slotTag.tagCount();
			if(j == 0) continue;
			
			while(--j >= 0)
			{
				NBTTagList moduleTag = (NBTTagList)slotTag.get(j);
				max = Math.max(
					max,
					1 + TagModular.getType(moduleTag).getDeepestPathNodeCount(moduleTag)
				);
			}
		}
		return max;
	}
	
	public boolean stream(NBTTagList tag, ModuleVisitor visitor)
	{
		// Visit this module
		if(visitor.visit(tag, this, null)) return true;
		
		// Visit each module installed on this module
		NBTTagList slotTag, moduleTag;
		for(int i = this.slots.length; i-- > 0; )
			for(int j = (slotTag = (NBTTagList)tag.get(1 + i)).tagCount(); j-- > 0; )
				if(
					TagModular.getType(
						moduleTag = (NBTTagList)slotTag.get(j)
					).stream(moduleTag, visitor)
				) return true;
		
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
	public final void stream(NBTTagList tag, CoordSystem thisSys, ModuleVisitor visitor)
	{
		// Visit this module
		visitor.visit(tag, this, thisSys);
		
		// Check if this module has slots to iterate
		if(this.slots.length == 0) return;
		
		// Fetch coordinate systems
		final CoordSystem moduleSys = CoordSystem.get();
		final CoordSystem slotSys = CoordSystem.get();
		
		// Go through each slot
		for(int i = this.slots.length; i-- > 0; )
		{
			// Check if there exists some installed modules
			NBTTagList slotTag = (NBTTagList)tag.get(1 + i);
			if(slotTag.tagCount() == 0) continue;
			
			// Setup coordinate system
			final Slot slot = this.slots[i];
			slotSys.set(thisSys);
			slotSys.trans(slot);
			if(slot.rotX != 0D)
				slotSys.rot(slot.rotX, CoordSystem.X);
			slotSys.submitRot();
			
			for(int j = slotTag.tagCount(); j-- > 0; )
			{
				NBTTagList moduleTag = (NBTTagList)slotTag.get(j);
				int[] states = TagModular.getStates(moduleTag);
				TypeModular moduleType = TagModular.getType(states);
				
				moduleSys.set(slotSys);
				moduleSys.trans(moduleType.getPosX(states, slot.stepLen), CoordSystem.NORM_X);
				moduleType.stream(moduleTag, moduleSys, visitor);
			}
		}
		
		slotSys.release();
		moduleSys.release();
	}
	
	public final int getStreamIndex(NBTTagList tag, byte[] loc, int len, int cursor)
	{
		if(cursor == len) return 0;
		
		int index = 1;
		int tarSlot = 0xFF & loc[cursor];
		int tarModule = 0xFF & loc[cursor + 1];
		
		NBTTagList slotTag, moduleTag;
		for(int i = this.slots.length; i-- > 0; )
			for(
				int j = (
					slotTag = (NBTTagList)tag.get(1 + i)
				).tagCount();
				--j >= 0;
			) {
				TypeModular type = TagModular.getType(
					moduleTag = (NBTTagList)slotTag.get(j)
				);
				if(i == tarSlot && j == tarModule)
					return index + type.getStreamIndex(moduleTag, loc, len, cursor + 2);
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
		for(int i = this.slots.length; i-- > 0; tag.appendTag(new NBTTagList()));
		return tag;
	}
	
	public final ResourceLocation getTexture(NBTTagList tag) {
		return this.getTexture(TagModular.getDam(tag));
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
		for(int i = this.slots.length; i-- > 0; )
			for(
				int j = (
					slotTag = (NBTTagList)tag.get(1 + i)
				).tagCount();
				--j >= 0;
				count += TagModular.getType(moduleTag = (NBTTagList)slotTag.get(j)).count(moduleTag)
			);
		
		return count;
	}
	
	protected void parseHitbox(String[] split, int index)
	{
		(
			this.hitbox[index] == DEF_HITBOXES
			? this.hitbox[index] = new Hitboxes()
			: this.hitbox[index]
		).parse(split, 1);
	}
	
	@FunctionalInterface
	public static interface ModuleVisitor
	{
		/**
		 * See {@link TypeModular#stream(ModuleVisitor, NBTTagList, CoordSystem)}
		 * 
		 * @param tag Tag of this module
		 * @param typ Type of this module
		 * @param sys
		 *     Position of this module. Note that this is just a buffer and it is likely to be
		 *     changed in the future. If you want to keep it please fetch a {@link CoordSystem}
		 *     instance and copy the value of this system to it. Changing position of this system
		 *     will have effect on all modules installed on it.
		 * @return {@code true} if should stop further streaming
		 */
		public boolean visit(NBTTagList tag, TypeModular typ, @Nullable CoordSystem sys);
	}
}
