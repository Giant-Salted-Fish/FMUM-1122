package com.fmum.item;

import com.google.gson.annotations.Expose;
import net.minecraft.item.ItemStack;

import java.util.Optional;

public class JsonItemStack
{
	@Expose
	protected String item_name;
	
	@Expose
	protected short item_meta = 0;
	
	@Expose
	protected int stack_size = 1;
	
	
	public JsonItemStack() { }
	
	public JsonItemStack( String item_name ) {
		this.item_name = item_name;
	}
	
	
	// TODO: Maybe NBT?
	public Optional< ItemStack > create()
	{
		return (
			IItemType.lookupItemFactory( this.item_name )
			.flatMap( factory -> {
				final ItemStack stack = factory.apply( this.item_meta );
				stack.setCount( this.stack_size );
				return stack.isEmpty() ? Optional.empty() : Optional.of( stack );
			} )
		);
	}
}
