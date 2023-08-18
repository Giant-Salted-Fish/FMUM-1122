package com.fmum.client.input;

import com.fmum.client.FMUMClient;
import com.fmum.client.input.IKeyBind.BindingState;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent.KeyInputEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent.MouseInputEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly( Side.CLIENT )
@EventBusSubscriber( modid = FMUMClient.MODID, value = Side.CLIENT )
public class KeyBindManager
{
	private static final HashMultimap< Integer, KeyBind >
		GLOBAL_TABLE = HashMultimap.create(),
		NORMAL_TABLE = HashMultimap.create(),
		CTRL_TABLE = HashMultimap.create(),
		SHIFT_TABLE = HashMultimap.create(),
		ALT_TABLE = HashMultimap.create();

	private static final HashMap< keyModifier, HashMultimap< Integer, KeyBind > >
		MODIFIER_2_UPDATE_TABLE = new HashMap<>();
	static
	{
		MODIFIER_2_UPDATE_TABLE.put( KeyModifier.NONE, NORMAL_TABLE );
		MODIFIER_2_UPDATE_TABLE.put( KeyModifier.CONTROL, )
	}

	private KeyBindManager() { }
	
	@SubscribeEvent
	static void onKeyboardInput( KeyInputEvent evt )
	{
	
	}
	
	@SubscribeEvent
	static void onMouseInput( MouseInputEvent evt )
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
			final BindingState state = kb.clearVanillaKeyBind();
			changed |= state == BindingState.CHANGED;
		}
		
		KeyBinding.resetKeyBindingArrayAndHash();
		
		if ( changed )
		{
		
		}
	}
}