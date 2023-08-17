package com.fmum.client.input;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly( Side.CLIENT )
public interface IKeyBind
{
	Registry< IKeyBind > REGISTRY = new Registry<>( IKeyBind::name );

	String name();

	String category();

	void activeUpdate( boolean is_down );

	void inactiveUpdate( boolean is_down );

	void restoreVanillaKeyBind();

	KeyBindState clearVanillaKeyBind();

	enum KeyBindState
	{
		BOUNDEN_KEY_CHANGED,
		BOUNDEN_KEY_UNCHANG;
	}
}