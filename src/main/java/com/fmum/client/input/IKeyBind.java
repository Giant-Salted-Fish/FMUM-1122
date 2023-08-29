package com.fmum.client.input;

import com.fmum.common.Registry;
import net.minecraftforge.client.settings.KeyModifier;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly( Side.CLIENT )
public interface IKeyBind
{
	Registry< IKeyBind > REGISTRY = new Registry<>( IKeyBind::identifier );
	
	String identifier();
	
	boolean isDown();
	
	int keyCode();
	
	KeyModifier keyModifier();
	
	void setKeyCodeAndModifier( int key_code, KeyModifier key_modifier );
	
	String boundenKeyRepr();
	
	void update( boolean is_down );
	
	void restoreVanillaKeyBind();
	
	boolean clearVanillaKeyBind();
}
