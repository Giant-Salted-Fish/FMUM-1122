package com.fmum.common.module;

import com.fmum.common.util.Vec3;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagList;

public class ModuleInfo extends Vec3
{
	public double
		sin = 0D,
		cos = 1D;
	
	public double rotX = 0D;
	
	public NBTTagList tag = null;
	
	public TypeModular type = null;
	
	public ModuleInfo() { }
	
	public ModuleInfo(NBTTagList tag, TypeModular type)
	{
		this.tag = tag;
		this.type = type;
	}
	
	public final ModuleInfo setDefault(ItemStack stack) {
		return this.setDefault(TagModular.getTag(stack), ((ItemModular)stack.getItem()).getType());
	}
	
	public final ModuleInfo setDefault(NBTTagList tag, TypeModular type)
	{
		this.tag = tag;
		this.type = type;
		this.set(0D);
		this.sin = 0D;
		this.cos = 1D;
		this.rotX = 0D;
		return this;
	}
	
	/**
	 * Update sin and cos value based the x-rotation that been set
	 * 
	 * @return {@code this}
	 */
	public final ModuleInfo updateSinAndCos()
	{
		this.sin = Math.sin(
			this.cos = Math.toRadians(this.rotX)
		);
		this.cos = Math.cos(this.cos);
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
	public final ModuleInfo moveTo(byte[] loc, int len)
	{
		for(int i = 0; i < len; i += 2)
		{
			Slot slot = this.type.slots[loc[i]];
			int[] states = (
				this.tag = ((NBTTagList)(
					(NBTTagList)this.tag.get(loc[i] + 1)
				).get(loc[i + 1]))
			).getIntArrayAt(0);
			
			this.x += slot.x + (
				this.type = TagModular.getType(states)
			).getPos(states, slot.stepLen);
			this.y += slot.y * this.cos - slot.z * this.sin;
			this.z += slot.y * this.sin + slot.z * this.cos;
			this.rotX += slot.rotX;
			this.updateSinAndCos();
		}
		return this;
	}
	
	/**
	 * Move one layer deep into the modifiable of required location
	 * 
	 * @param slot Slot that target modifiable located in
	 * @param index Index of the modifiable
	 * @return {@code this}
	 */
	public final ModuleInfo moveTo(int slot, int index)
	{
		Slot s = this.type.slots[slot];
		int[] states = (
			this.tag = ((NBTTagList)(
				(NBTTagList)this.tag.get(slot + 1)
			).get(index))
		).getIntArrayAt(0);
		
		this.x += s.x + (
			this.type = TagModular.getType(states)
		).getPos(states, s.stepLen);
		this.y += s.y * this.cos - s.z * this.sin;
		this.z += s.y * this.sin + s.z * this.cos;
		this.rotX += s.rotX;
		this.updateSinAndCos();
		return this;
	}
	
	/**
	 * Try to move to given location. It validates the given location and length to make sure
	 * that this method never crash. Every byte of the location should be valid and length
	 * should be multiple of 2 and not negative.
	 * 
	 * @param location Location of the target modifiable
	 * @param len Length of the location
	 * @return {@code null} if any error occurred in the progress
	 */
	public final ModuleInfo tryMoveTo(byte[] loc, int len)
	{
		if(len < 0 || (len & 1) > 0) return null;
		
		for(int i = 0; i < len; i += 2)
		{
			if((0xFF & loc[i]) >= this.type.slots.length) return null;
			Slot slot = this.type.slots[loc[i]];
			this.tag = ((NBTTagList)this.tag.get(loc[i] + 1));
			
			if((0xFF & loc[i + 1]) >= this.tag.tagCount()) return null;
			int[] states = (
				this.tag = ((NBTTagList)this.tag.get(loc[i + 1]))
			).getIntArrayAt(0);
			
			this.x += slot.x + (
				this.type = TagModular.getType(states)
			).getPos(states, slot.stepLen);
			this.y += slot.y * this.cos - slot.z * this.sin;
			this.z += slot.y * this.sin + slot.z * this.cos;
			this.rotX += slot.rotX;
			this.updateSinAndCos();
		}
		return this;
	}
	
	public void apply(Vec3 raw, Vec3 dest)
	{
		dest.x = raw.x + this.x;
		double y = this.y + raw.y * this.cos - raw.z * this.sin;
		dest.z = this.z + raw.y * this.sin + raw.z * this.cos;
		dest.y = y;
	}
}
