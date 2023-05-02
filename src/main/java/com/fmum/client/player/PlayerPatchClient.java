package com.fmum.client.player;

import static net.minecraft.util.EnumHand.MAIN_HAND;
import static net.minecraft.util.EnumHand.OFF_HAND;

import org.lwjgl.opengl.GL11;

import com.fmum.client.EventHandlerClient;
import com.fmum.client.FMUMClient;
import com.fmum.client.camera.CameraAnimator;
import com.fmum.client.camera.ICameraController;
import com.fmum.client.input.IInput;
import com.fmum.client.render.Model;
import com.fmum.common.player.PlayerPatch;
import com.fmum.util.Mat4f;
import com.fmum.util.Vec3f;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumHand;
import net.minecraft.util.MouseHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly( Side.CLIENT )
public final class PlayerPatchClient extends PlayerPatch
{
	/**
	 * As there could only be one client player hence only one client patch instance here.
	 */
	public static PlayerPatchClient instance;
	
	/**
	 * Player's eye position.
	 */
	public final Vec3f
		playerPos = new Vec3f(),
		prevPlayerPos = new Vec3f();
	
	/**
	 * Player's raw velocity(referenced to player's eyes).
	 */
	public final Vec3f
		playerVelocity = new Vec3f(),
		prevPlayerVelocity = new Vec3f();
	
	/**
	 * Player's raw acceleration(referenced to player's eyes).
	 */
	public final Vec3f
		playerAcceleration = new Vec3f(),
		prevPlayerAcceleration = new Vec3f();
	
	// TODO: maybe wrapper this
	public ICameraController camera = CameraAnimator.INSTANCE;
	
	/**
	 * <p> Hook mouse change method to do pre-render work. </p>
	 * 
	 * <p> Notice that this is kind of hack so you may need to check before you use it in other
	 * version of {@link Minecraft}. </p>
	 */
	private final MouseHelper mouseHelper = new MouseHelper()
	{
		@Override
		public void mouseXYChange()
		{
			super.mouseXYChange();
			
			// This method is called right before the render and player rotation is also updated. \
			// Hence is the proper place to fire prepare render callback.
			final PlayerPatchClient patch = PlayerPatchClient.this;
			patch.mainEquipped.updateAnimationForRender( MAIN_HAND );
			patch.offEquipped.updateAnimationForRender( OFF_HAND );
			
			patch.camera.prepareRender( this );
			patch.mainEquipped.prepareRenderInHandSP( MAIN_HAND );
			patch.offEquipped.prepareRenderInHandSP( OFF_HAND );
		}
	};
	
	public PlayerPatchClient( EntityPlayer player )
	{
		super( player );
		
		instance = this;
	}
	
	@Override
	public void tick()
	{
		super.tick();
		
		// TODO: can only update by setting a runner in gui event
		// Only update game settings when there is no GUI activated.
		if ( FMUMClient.MC.currentScreen == null )
		{
			final GameSettings settings = FMUMClient.SETTINGS;
			final boolean flag = EventHandlerClient.oriViewBobbing;
			settings.viewBobbing = this.mainEquipped.updateViewBobbing( flag );
		}
		
		// Update player velocity and acceleration.
		this.prevPlayerPos.set( this.playerPos );
		this.playerPos.set(
			( float ) this.player.posX,
			( float ) this.player.posY + this.player.getEyeHeight(),
			( float ) this.player.posZ
		);
		
		this.prevPlayerVelocity.set( this.playerVelocity );
		this.playerVelocity.set( this.playerPos );
		this.playerVelocity.sub( this.prevPlayerPos );
		
		this.prevPlayerAcceleration.set( this.playerAcceleration );
		this.playerAcceleration.set( this.playerVelocity );
		this.playerAcceleration.sub( this.prevPlayerVelocity );
		
		// Update camera effects.
		this.camera.tick();
		
		// Ensure mouse helper(Mods like Flan's Mod may change mouse helper in certain conditions).
		// TODO: maybe do a wrapper if the mouse help is not the original one and also not this one
		FMUMClient.MC.mouseHelper = this.mouseHelper;
	}
	
	public boolean onRenderHandSP()
	{
		// Check if hand should be rendered or not.
		// Copied from {@link EntityRenderer#renderHand(float, int)}.
		final Minecraft mc = FMUMClient.MC;
		final GameSettings settings = FMUMClient.SETTINGS;
		final Entity entity = mc.getRenderViewEntity();
		if (
			settings.thirdPersonView != 0
			|| entity instanceof EntityLivingBase
				&& ( ( EntityLivingBase ) entity ).isPlayerSleeping()
			|| settings.hideGUI
			|| mc.playerController.isSpectator()
			|| this.mainEquipped.renderInHandSP( MAIN_HAND )
				&& this.offEquipped.renderInHandSP( OFF_HAND )
		) { return true; }
		
		// Otherwise, setup orientation for vanilla item rendering.
		final Mat4f mat = Mat4f.locate();
		this.camera.getViewTransform( mat );
		Model.glMulMatrix( mat );
		mat.release();
		
		GL11.glRotatef( 180F - this.player.rotationYaw, 0F, 1F, 0F );
		GL11.glRotatef( -this.player.rotationPitch, 1F, 0F, 0F );
		return false;
	}
	
	public boolean onRenderSpecificHandSP( EnumHand hand )
	{
		final boolean isOffHand = hand == OFF_HAND;
		return ( isOffHand ? this.offEquipped : this.mainEquipped ).onRenderSpecificHandSP( hand );
	}
	
	public boolean hideCrosshair() { return this.mainEquipped.hideCrosshair(); }
	
	public boolean onMouseWheelInput( int dWheel ) {
		return this.mainEquipped.onMouseWheelInput( dWheel );
	}
	
	public void onKeyPress( IInput key ) { this.mainEquipped.onKeyPress( key ); }
	
	public void onKeyRelease( IInput key ) { this.mainEquipped.onKeyRelease( key ); }
}
