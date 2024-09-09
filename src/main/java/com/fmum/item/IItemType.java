package com.fmum.item;

import com.fmum.FMUM;
import com.fmum.Registry;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.util.Optional;
import java.util.function.Function;

/**
 * <p>This corresponds to the concept of {@link Item} in Minecraft.</p>
 *
 * <p>Notice that there may not exist a one-to-one mapping between this and
 * {@link Item}. Multiple {@link IItemType} may share the same {@link Item} and
 * differ in meta or NBT data. Hence, {@code new ItemStack(stack.getItem())} may
 * not produce correct result and should not be used.</p>
 */
public interface IItemType
{
	Registry< IItemType > REGISTRY = new Registry<>();
	
	
	String getName();
	
	/**
	 * A factory method that creates an {@link ItemStack} of this type. In most
	 * cases this will be the only recommended way to create {@link ItemStack}
	 * instance for items in {@link FMUM}. {@link ItemStack#ItemStack(Item)}
	 * does not guarantee to work for {@link IItemType} items.
	 */
	ItemStack newItemStack( short meta );
	
	
	static Optional< Function< Short, ItemStack > > lookupItemFactory( String name_or_id )
	{
		return (
			REGISTRY.lookup( name_or_id )
			.map( t -> ( Function< Short, ItemStack > ) t::newItemStack )
			.map( Optional::of )
			.orElseGet( () -> (
				Optional.ofNullable( Item.getByNameOrId( name_or_id ) )
				.map( item -> meta -> new ItemStack( item, 1, meta ) )
			) )
		);
	}
}
