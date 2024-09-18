package com.fmum.ammo;

import com.fmum.BiRegistry;
import com.fmum.item.IItem;
import com.fmum.item.IItemType;
import com.fmum.item.ItemCategory;
import com.fmum.render.IAnimator;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Optional;
import java.util.OptionalInt;
import java.util.function.Predicate;

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
		OptionalInt slot = OptionalInt.empty();
		final int size = inv.getSizeInventory();
		for ( int i = 0; i < size; i += 1 )
		{
			final ItemStack stack = inv.getStackInSlot( i );
			final Optional< IAmmoType > ammo = (
				IItem.ofOrEmpty( stack )
				.map( IItem::getType )
				.filter( IAmmoType.class::isInstance )
				.map( IAmmoType.class::cast )
			);
			if ( ammo.isPresent() && filter.test( ammo.get() ) )
			{
				slot = OptionalInt.of( i );
				
				offset -= 1;
				if ( offset < 0 ) {
					break;
				}
			}
		}
		return slot;
	}
}
