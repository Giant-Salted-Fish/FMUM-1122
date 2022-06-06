package com.fmum.common.weapon.gun;

import java.util.Set;

import com.fmum.common.item.MetaItem;
import com.fmum.common.module.MetaModular;
import com.fmum.common.paintjob.MetaPaintable;
import com.fmum.common.weapon.MetaAmmoContainer;

/**
 * Abstract of any part of a gun, including gun parts, magazines and attachments. They should be
 * modular and can be paint
 * 
 * @author Giant_Salted_Fish
 */
public interface MetaGunPart extends MetaItem, MetaModular, MetaPaintable, MetaAmmoContainer
{
	@Override
	public default void regisPostInitHandler( Set< Runnable > tasks )
	{
		MetaItem.super.regisPostInitHandler( tasks );
		MetaModular.super.regisPostInitHandler( tasks );
		MetaPaintable.super.regisPostInitHandler( tasks );
		MetaAmmoContainer.super.regisPostInitHandler( tasks );
	}
	
	@Override
	public default void regisPostLoadHandler( Set< Runnable > tasks )
	{
		MetaItem.super.regisPostLoadHandler( tasks );
		MetaModular.super.regisPostLoadHandler( tasks );
		MetaPaintable.super.regisPostLoadHandler( tasks );
		MetaAmmoContainer.super.regisPostLoadHandler( tasks );
	}
}
