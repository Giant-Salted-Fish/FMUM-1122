package com.fmum.ammo;

import com.fmum.BiRegistry;
import com.fmum.animation.IAnimator;
import com.fmum.item.IItem;
import com.fmum.item.IItemType;
import com.fmum.item.ItemCategory;
import net.minecraft.inventory.IInventory;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.OptionalInt;
import java.util.PrimitiveIterator.OfInt;
import java.util.function.Predicate;
import java.util.stream.IntStream;

public interface IAmmoType extends IItemType
{
	BiRegistry< Short, IAmmoType > REGISTRY = BiRegistry.createWithShortKey();
	
	
	ItemCategory getCategory();
	
	boolean canShoot();
	
	IAmmoType prepareShoot();
	
	@SideOnly( Side.CLIENT )
	void renderModel( IAnimator animator );
	
	
	static OptionalInt lookupValidAmmoSlot(
		IInventory inv,
		Predicate< ? super IAmmoType > filter,
		int offset
	) {
		final OfInt itr = (
			IntStream.range( 0, inv.getSizeInventory() )
			.filter( i -> (
				IItem.ofOrEmpty( inv.getStackInSlot( i ) )
				.map( IItem::getType )
				.filter( IAmmoType.class::isInstance )
				.map( IAmmoType.class::cast )
				.filter( filter )
				.isPresent()
			) )
			.iterator()
		);
		
		OptionalInt slot = OptionalInt.empty();
		while ( itr.hasNext() && offset >= 0 )
		{
			slot = OptionalInt.of( itr.nextInt() );
			offset -= 1;
		}
		return slot;
	}
}
