package com.fmum.client.input;

import com.fmum.common.Registry;
import net.minecraftforge.client.settings.KeyModifier;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly( Side.CLIENT )
public interface KeyBind
{
	Registry< KeyBind > REGISTRY = new Registry<>( KeyBind::identifier );
	
	String identifier();
	
	int keyCode();
	
	KeyModifier keyModifier();
	
	void update( boolean is_down );
	
	void setKeyCodeAndModifier( int key_code, KeyModifier key_modifier );
	
	void restoreVanillaKeyBind();
	
	BindingState clearVanillaKeyBind();
	
	enum BindingState
	{
		CHANGED,
		UNCHANGED,
	}
}
