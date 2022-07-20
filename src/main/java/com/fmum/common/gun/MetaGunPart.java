package com.fmum.common.gun;

import java.util.Map;

import com.fmum.common.item.MetaItem;
import com.fmum.common.module.MetaModular;
import com.fmum.common.module.ModuleSlot;
import com.fmum.common.paintjob.MetaPaintable;
import com.fmum.common.util.CoordSystem;
import com.fmum.common.weapon.MetaAmmoContainer;

import net.minecraft.nbt.NBTTagList;

/**
 * <p> Abstract of any part of a gun, including gun parts, magazines and attachments. They should be
 * modular and can be paint. It supports to adjust the install step and install offset. </p>
 * 
 * <p> This requires 1 extra {@code int} data to save step, offset.
 * <pre>
 *    8     2           22
 * 00000000 00 0000000000000000000000
 *    |     |           |
 *   step   |         unused
 *        offset
 * </pre>
 * 
 * FIXME: save ammo info into tag & check if dataSize used as index is valid
 * 
 * @author Giant_Salted_Fish
 */
public interface MetaGunPart extends MetaItem, MetaModular, MetaPaintable, MetaAmmoContainer
{
	@Override
	public default void regisPostInitHandler( Map< String, Runnable > tasks )
	{
		MetaItem.super.regisPostInitHandler( tasks );
		MetaModular.super.regisPostInitHandler( tasks );
		MetaPaintable.super.regisPostInitHandler( tasks );
		MetaAmmoContainer.super.regisPostInitHandler( tasks );
	}
	
	@Override
	public default void regisPostLoadHandler( Map< String, Runnable > tasks )
	{
		MetaItem.super.regisPostLoadHandler( tasks );
		MetaModular.super.regisPostLoadHandler( tasks );
		MetaPaintable.super.regisPostLoadHandler( tasks );
		MetaAmmoContainer.super.regisPostLoadHandler( tasks );
	}
	
	@Override
	public default void apply( NBTTagList tag, ModuleSlot slot, CoordSystem dst )
	{
		int[] data = MetaModular.getDataArray( tag );
		int offset = MetaModular.super.dataSize();
		dst.trans(
			slot.posStep() * MetaModular.getData( data, offset, 0, 0xFF )
				+ this.offset( MetaModular.getData( data, offset, 8, 3 ) ),
			CoordSystem.NORM_X
		);
	}
	
	@Override
	default int step( NBTTagList tag ) {
		return MetaModular.getData( tag, MetaModular.super.dataSize(), 0, 0xFF );
	}
	
	@Override
	public default void $step( NBTTagList tag, int step ) {
		MetaModular.setData( tag, MetaModular.super.dataSize(), 0, 0xFF, step );
	}
	
	@Override
	default int offset( NBTTagList tag ) {
		return MetaModular.getData( tag, MetaModular.super.dataSize(), 8, 3 );
	}
	
	@Override
	public default void $offset( NBTTagList tag, int offset ) {
		MetaModular.setData( tag, MetaModular.super.dataSize(), 8, 3, offset );
	}
	
	@Override
	public default int dataSize() { return MetaModular.super.dataSize() + 1; }
}
