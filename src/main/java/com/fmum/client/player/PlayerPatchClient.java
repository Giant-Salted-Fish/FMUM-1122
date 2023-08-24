package com.fmum.client.player;

import com.fmum.client.input.Input;
import com.fmum.common.player.PlayerPatch;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly( Side.CLIENT )
public final class PlayerPatchClient extends PlayerPatch
{
	private static PlayerPatchClient instance;
	
	public PlayerPatchClient() {
		instance = this;
	}
	
	public void onInputSignal( String signal, Input input ) {
		this.main_equipped.onInputSignal( signal, input );
	}
	
	public static PlayerPatchClient get() { return instance; }
}
