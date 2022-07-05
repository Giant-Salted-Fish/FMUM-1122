package com.fmum.common.gun;

import java.util.Map;

import com.fmum.common.item.TypeItemCustomizable;
import com.fmum.common.util.LocalAttrParser;

public class TypeGun extends TypeItemCustomizable implements MetaGun
{
	public static final LocalAttrParser< TypeGun >
		parser = new LocalAttrParser<>( TypeGun.class, TypeItemCustomizable.parser );
	static
	{
		
	}
	
	public TypeGun( String name ) { super( name ); }
	
	@Override
	public void regisPostInitHandler( Map< String, Runnable > tasks )
	{
		super.regisPostInitHandler( tasks );
		MetaGun.super.regisPostInitHandler( tasks );
	}
	
	@Override
	public void regisPostLoadHandler( Map< String, Runnable > tasks )
	{
		super.regisPostLoadHandler( tasks );
		MetaGun.super.regisPostLoadHandler( tasks );
	}
	
	@Override
	protected void setupItem() { this.withItem( new ItemGunBase( this ) ); }
}
