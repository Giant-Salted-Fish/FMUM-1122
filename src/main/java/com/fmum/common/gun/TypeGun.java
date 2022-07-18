package com.fmum.common.gun;

import java.util.Map;

import com.fmum.client.item.RenderableItem;
import com.fmum.common.meta.EnumMeta;
import com.fmum.common.pack.TypeParser;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

// FIXME: replace RenderableItem & maybe set a default model here?
public class TypeGun extends TypeGunPart< RenderableItem > implements MetaGun
{
	public static final TypeParser< TypeGun >
		parser = new TypeParser<>( TypeGun.class, TypeGunPart.parser );
	static
	{
	}
	
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
	public EnumMeta enumMeta() { return MetaGun.super.enumMeta(); }
	
	@Override
	protected void createItem() { this.withItem( new ItemGunBase( this ), 1, 0 ); }
	
	@Override
	@SideOnly( Side.CLIENT )
	protected Class< ? extends RenderableItem > requiredModelClass() {
		return RenderableItem.class; // TODO
	}
}
