package com.mcwb.client.ammo;

import com.mcwb.client.item.ItemRenderer;
import com.mcwb.client.render.IRenderer;
import com.mcwb.common.MCWB;
import com.mcwb.common.ammo.IAmmoType;
import com.mcwb.common.item.IItem;
import com.mcwb.common.load.BuildableLoader;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly( Side.CLIENT )
public class AmmoRenderer< T extends IAmmoType, C extends IItem > extends ItemRenderer< C >
	implements IAmmoRenderer< T, C >
{
	public static final BuildableLoader< IRenderer > LOADER
		= new BuildableLoader<>( "ammo", json -> MCWB.GSON.fromJson( json, AmmoRenderer.class ) );
	
	@Override
	public void render( T type )
	{
		this.bindTexture( type.texture() );
		this.render();
	}
}
