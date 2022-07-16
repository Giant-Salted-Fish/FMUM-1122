package com.fmum.common.gun;

import java.util.Map;

import com.fmum.client.item.ModelItem;
import com.fmum.client.item.RenderableItem;
import com.fmum.common.item.TypeItemCustomizable;
import com.fmum.common.meta.EnumMeta;
import com.fmum.common.pack.TypeParser;

// FIXME: replace RenderableItem & maybe set a default model here?
public class TypeGun extends TypeItemCustomizable< RenderableItem >
	implements MetaGun
{
	public static final TypeParser< TypeGun >
		parser = new TypeParser<>( TypeGun.class, TypeItemCustomizable.parser );
	static
	{
		// FIXME: load model
	}
	
//	protected static final ModelItem DEF_MODEL = new ModelItem();
	
	public TypeGun( String name )
	{
		super( name );
		
		// Set a default model to make sure it will not crash if failed to load the model
//		this.model = DEF_MODEL;
	}
	
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
	protected void createItem() { this.withItem( new ItemGunBase( this ), 1, 0 ); }
}
