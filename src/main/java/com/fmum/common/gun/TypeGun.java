package com.fmum.common.gun;

import java.util.Map;

import com.fmum.common.item.TypeItemCustomizable;
import com.fmum.common.meta.EnumMeta;
import com.fmum.common.pack.TypeParser;

public class TypeGun extends TypeItemCustomizable implements MetaGun
{
	public static final TypeParser< TypeGun >
		parser = new TypeParser<>( TypeGun.class, TypeItemCustomizable.parser );
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
	public EnumMeta enumMeta() { return EnumMeta.GUN; }
	
	@Override
	protected void createItem() { this.withItem( new ItemGunBase( this ) ); }
}
