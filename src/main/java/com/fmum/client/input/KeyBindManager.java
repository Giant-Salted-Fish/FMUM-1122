package com.fmum.client.input;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly( Side.CLIENT )
@EventBusSubscriber( modid = FMUMClient.MODID, value = Side.CLIENT )
public class KeyBindManager
{
	private KeyBindManager() { }

	@EventSubscriber
	static void onKeyboardInput( KeyInputEvent evt )
	{

	}

	@EventSubscriber
	static void onMouseInput( MouserInputEvent )
	{

	}

	public static void restoreMcKeyBind()
	{
		IKeyBind.REGISTRY.values().forEach( IKeyBind::restoreVanillaKeyBind );
	}

	public static void clearMcKeyBind()
	{
		boolean changed = false;
		for ( IKeyBind kb : IKeyBind.REGISTRY.values() )
		{
			final KeyBIndState state = kb.clearVanillaKeyBind();
			changed |= state == KeyBIndState.BOUNDEN_KEY_CHANGED;
		}

		KeyBinding.resetKeyBindingArrayAndHash();

		if ( changed )
		{
			
		}
	}
}