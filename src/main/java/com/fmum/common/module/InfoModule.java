package com.fmum.common.module;

import com.fmum.common.util.CoordSystem;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagList;

public class InfoModule
{
	public NBTTagList tag = null;
	
	public TypeModular type = null;
	
	public CoordSystem sys = new CoordSystem();
	
	public InfoModule() { }
	
	public InfoModule(ItemStack stack)
	{
		this.tag = TagModular.getTag(stack);
		this.type = ((ItemModular)stack.getItem()).getType();
	}
	
	public final InfoModule setDefault(ItemStack stack) {
		return this.setDefault(TagModular.getTag(stack), ((ItemModular)stack.getItem()).getType());
	}
	
	public final InfoModule setDefault(NBTTagList tag, TypeModular type)
	{
		this.tag = tag;
		this.type = type;
		this.sys.setDefault();
		return this;
	}
	
	/**
	 * Locate modifiable information from current modifiable
	 * 
	 * @note Make sure you set {@link #tag} and {@link #type} before calling this method
	 * 
	 * @param loc Location of the attachment
	 * @param len Depth to go upon location
	 * @return {@code this} with target modifiable's x, y, z, x-rot, sin, cos, atTag
	 */
	public final InfoModule moveTo(byte[] loc, int len)
	{
		for(int i = 0; i < len; i += 2)
			this.moveTo(0xFF & loc[i], 0xFF & loc[i + 1]);
		return this;
	}
	
	/**
	 * Move one layer deep into the modifiable of required location
	 * 
	 * @param slot Slot that target modifiable located in
	 * @param index Index of the modifiable
	 * @return {@code this}
	 */
	public final InfoModule moveTo(int slot, int index)
	{
		Slot s = this.type.slots[slot];
		int[] states = TagModular.getStates(
			this.tag = ((NBTTagList)(
				(NBTTagList)this.tag.get(1 + slot)
			).get(index))
		);
		
		this.sys.trans(
			s.x + (
				this.type = TagModular.getType(states)
			).getPos(states, s.stepLen),
			s.y,
			s.z
		);
		this.sys.rot(s.rotX, CoordSystem.X);
		this.sys.submitRot();
		return this;
	}
	
	/**
	 * Try to move to given location. It validates the given location and length to make sure
	 * that this method never crash. Every byte of the location should be valid.
	 * 
	 * @param loc Location of the target module
	 * @param len Length of the location
	 * @return {@code null} if any error occurred in the progress
	 */
	public final InfoModule tryMoveTo(byte[] loc, int len)
	{
		for(int i = 0; i < len; i += 2)
			if(this.tryMoveTo(0xFF & loc[i], 0xFF & loc[i + 1]) == null)
				return null;
		return this;
	}
	
	/**
	 * @see #tryMoveTo(byte[], int)
	 */
	public final InfoModule tryMoveTo(int slot, int index)
	{
		if(slot >= this.type.slots.length) return null;
		Slot s = this.type.slots[slot];
		NBTTagList slotTag = (NBTTagList)this.tag.get(1 + slot);
		
		if(index >= slotTag.tagCount()) return null;
		int[] states = TagModular.getStates(
			this.tag = (NBTTagList)slotTag.get(index)
		);
		
		this.sys.trans(
			s.x + (
				this.type = TagModular.getType(states)
			).getPos(states, s.stepLen),
			s.y,
			s.z
		);
		this.sys.rot(s.rotX, CoordSystem.X);
		this.sys.submitRot();
		return this;
	}
}
