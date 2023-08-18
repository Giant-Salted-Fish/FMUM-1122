package com.fmum.client.input;

import com.fmum.common.Registry;
import net.minecraftforge.client.settings.KeyModifier;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly( Side.CLIENT )
public interface IKeyBind
{
	Registry< IKeyBind > REGISTRY = new Registry<>();
	
	boolean isDown();
	
	int keyCode();
	
	KeyModifier keyModifier();
	
	void activeUpdate( boolean is_down );
	
	void inactiveUpdate( boolean is_down );
	
	void restoreVanillaKeyBind();
	
	BindingState clearVanillaKeyBind();
	
	enum BindingState
	{
		CHANGED,
		UNCHANGED;
	}
}