package com.fmum.ammo;

import com.fmum.BiRegistry;
import com.fmum.item.IItem;
import com.fmum.item.IItemType;
import com.fmum.item.ItemCategory;
import gsf.util.animation.IAnimator;
import net.minecraft.inventory.IInventory;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.OptionalInt;
import java.util.PrimitiveIterator;
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
		final PrimitiveIterator.OfInt itr = (
			IItem.lookupIn( inv, it -> {
				final IItemType type = it.getType();
				final boolean is_ammo = type instanceof IAmmoType;
				return is_ammo && filter.test( ( IAmmoType ) type );
			} )
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
