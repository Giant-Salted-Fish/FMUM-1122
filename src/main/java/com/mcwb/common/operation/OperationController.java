package com.mcwb.common.operation;

import com.google.gson.JsonDeserializer;
import com.mcwb.common.MCWB;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;

public class OperationController implements IOperationController
{
	public static final JsonDeserializer< IOperationController > ADAPTER =
		( json, typeOfT, context ) -> MCWB.GSON.fromJson( json, OperationController.class );
	
	protected static final float[] TIME_ARR = { };
	protected static final String[] EFFECT_ARR = { };
	protected static final SoundEvent[] SOUNDS = { };
	
	protected float progressor = 0.1F;
	
	protected float[] effectTime = TIME_ARR;
	protected String[] effects = EFFECT_ARR;
	
	protected float[] soundTime = TIME_ARR;
	protected SoundEvent[] sounds = SOUNDS;
	
	public OperationController() { }
	
	public OperationController( float progress ) { this.progressor = progress; }
	
	public OperationController(
		float progressor,
		float[] effectTime,
		String[] effects,
		float[] soundTime,
		String... sounds
	) {
		this.progressor = progressor;
		this.effectTime = effectTime;
		this.effects = effects;
		this.soundTime = soundTime;
		this.sounds = new SoundEvent[ sounds.length ];
		for ( int i = sounds.length; i-- > 0; this.sounds[ i ] = MCWB.MOD.loadSound( sounds[ i ] ) );
	}
	
	@Override
	public float progressor() { return this.progressor; }
	
	@Override
	public int effectCount() { return this.effectTime.length; }
	
	@Override
	public float getEffectTime( int idx ) { return this.effectTime[ idx ]; }
	
	@Override
	public String getEffect( int idx ) { return this.effects[ idx ]; }
	
	@Override
	public int soundCount() { return this.soundTime.length; }
	
	@Override
	public float getSoundTime( int idx ) { return this.soundTime[ idx ]; }
	
	@Override
	public void handlePlaySound( int idx, EntityPlayer player )
	{
		// TODO: proper handling of this
		player.world.playSound(
			player.posX, player.posY, player.posZ,
			this.sounds[ idx ],
			SoundCategory.PLAYERS,
			1F, 1F, false
		);
	}
}
