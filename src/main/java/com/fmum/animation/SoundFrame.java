package com.fmum.animation;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;

public class SoundFrame
{
	public final float time;
	public final SoundEvent sound;
	
	public SoundFrame( float time, SoundEvent sound )
	{
		this.time = time;
		this.sound = sound;
	}
	
	public static int playSound( SoundFrame[] frames, int from_idx, float progress, EntityPlayer player )
	{
		while ( from_idx < frames.length )
		{
			final SoundFrame frame = frames[ from_idx ];
			if ( frame.time > progress ) {
				break;
			}
			
			player.world.playSound(
				player.posX, player.posY, player.posZ,
				frame.sound, SoundCategory.PLAYERS,
				1.0F, 1.0F, false
			);
			from_idx += 1;
		}
		return from_idx;
	}
}
