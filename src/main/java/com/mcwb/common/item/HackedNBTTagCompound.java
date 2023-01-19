package com.mcwb.common.item;

import java.util.function.Supplier;

import com.mcwb.common.item.ModifiableItemMeta.ModifiableItem;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

/**
 * A specialized hack version of compound tag that used to handle the tag bind problem appear on
 * {@link ItemStack#copy()}
 * 
 * @see ModifiableItem#initCapabilities(ItemStack, NBTTagCompound)
 * @author Giant_Salted_Fish
 */
public class HackedNBTTagCompound extends NBTTagCompound
{
	public HackedNBTTagCompound() { }
	
	/**
	 * @param from
	 *     It simply set the tags in the given compound tag hence the given tag should never be
	 *     used after this call
	 */
	public HackedNBTTagCompound( NBTTagCompound from ) {
		for( final String key : from.getKeySet() ) this.setTag( key, from.getTag( key ) );
	}
	
	protected Supplier< NBTTagCompound > copyHandler = this::copy;
	
	public void setCopyDelegate( NBTTagCompound delegate )
	{
		this.copyHandler = () -> {
			this.copyHandler = this::copy;
			return delegate;
		};
	}
	
	/**
	 * Delegate instance if has set
	 */
	@Override
	public NBTTagCompound copy() { return this.copyHandler.get(); }
}
