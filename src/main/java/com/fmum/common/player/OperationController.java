package com.fmum.common.player;

import com.fmum.common.FMUM;
import com.fmum.common.load.IContentProvider;
import com.fmum.util.Animation;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Optional;

public class OperationController
{
	@SideOnly( Side.CLIENT )
	protected Animation animation;
	
	protected float progressor = 0.1F;
	
	protected TimedEffect[] effects = { };
	
	protected TimedSound[] sounds = { };
	
	public OperationController() { }
	
	public OperationController( float progress ) { this.progressor = progress; }
	
	public OperationController( float progressor, TimedEffect[] effects, TimedSound[] sounds )
	{
		this.progressor = progressor;
		this.effects = effects;
		this.sounds = sounds;
	}
	
	public float progressor() { return this.progressor; }
	
	public int effectCount() { return this.effects.length; }
	
	public float getEffectTime( int idx ) { return this.effects[ idx ].time; }
	
	public String getEffect( int idx ) { return this.effects[ idx ].effect; }
	
	public int soundCount() { return this.sounds.length; }
	
	public float getSoundTime( int idx ) { return this.sounds[ idx ].time; }
	
	public void handlePlaySound( int idx, EntityPlayer player )
	{
		// TODO: proper handling of this
		player.world.playSound(
			player.posX, player.posY, player.posZ,
			this.sounds[ idx ].sound,
			SoundCategory.PLAYERS,
			1F, 1F, false
		);
	}
	
	@SideOnly( Side.CLIENT )
	public Animation animation() { return this.animation; }
	
	@SideOnly( Side.CLIENT )
	public void checkAssetsSetup( IContentProvider provider ) {
		this.animation = Optional.ofNullable( this.animation ).orElse( Animation.NONE );
	}
	
	public static class TimedSound
	{
		public final float time;
		public final SoundEvent sound;
		
		public TimedSound( float time, String soundPath )
		{
			this.time = time;
			this.sound = FMUM.MOD.loadSound( soundPath );
		}
	}
	
	public static class TimedEffect
	{
		public final float time;
		public final String effect;
		
		public TimedEffect( float time, String effect )
		{
			this.time = time;
			this.effect = effect;
		}
	}
}
