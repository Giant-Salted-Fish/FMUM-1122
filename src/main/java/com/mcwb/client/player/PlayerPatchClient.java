package com.mcwb.client.player;

import org.lwjgl.opengl.GL11;

import com.mcwb.client.EventHandlerClient;
import com.mcwb.client.MCWBClient;
import com.mcwb.client.camera.CameraAnimator;
import com.mcwb.client.camera.ICameraController;
import com.mcwb.client.input.IKeyBind;
import com.mcwb.common.IAutowirePacketHandler;
import com.mcwb.common.item.IInUseItem;
import com.mcwb.common.network.PacketCode;
import com.mcwb.common.network.PacketCode.Code;
import com.mcwb.common.player.PlayerPatch;
import com.mcwb.util.Vec3f;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumHand;
import net.minecraft.util.MouseHelper;
import net.minecraftforge.client.event.EntityViewRenderEvent.CameraSetup;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Can only have one instance at one time for client player
 * 
 * @author Giant_Salted_Fish
 */
@SideOnly( Side.CLIENT )
public final class PlayerPatchClient extends PlayerPatch implements IAutowirePacketHandler
{
	/**
	 * As there could only be one client player hence only one client patch instance here
	 */
	public static PlayerPatchClient instance;
	
	/**
	 * Player's eye position
	 */
	public final Vec3f
		playerPos = new Vec3f(),
		prevPlayerPos = new Vec3f();
	
	/**
	 * Player's raw velocity(referenced to player's eyes)
	 */
	public final Vec3f
		playerVelocity = new Vec3f(),
		prevPlayerVelocity = new Vec3f();
	
	/**
	 * Player's raw acceleration(referenced to player's eyes)
	 */
	public final Vec3f
		playerAcceleration = new Vec3f(),
		prevPlayerAcceleration = new Vec3f();
	
	// TODO: maybe wrapper this
	public ICameraController cameraController = CameraAnimator.INSTANCE;
	
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
			patch.cameraController.prepareRender( this );
			patch.mainItem.prepareRenderInHand( EnumHand.MAIN_HAND );
			patch.offItem.prepareRenderInHand( EnumHand.OFF_HAND );
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
		// Only update game settings when there is no GUI activated
		if( MCWBClient.MC.currentScreen == null )
		{
			final GameSettings settings = MCWBClient.SETTINGS;
			settings.viewBobbing = this.mainItem
				.updateViewBobbing( EventHandlerClient.oriViewBobbing );
		}
		
		// Update player velocity and acceleration
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
		
		// Update camera effects
		this.cameraController.tick();
		
		// Ensure mouse helper(Mods like Flan's Mod may change mouse helper in certain conditions)
		// TODO: maybe do a wrapper if the mouse help is not the original one and also not this one
		MCWBClient.MC.mouseHelper = this.mouseHelper;
	}
	
	public boolean onRenderHand()
	{
		// Check if hand should be rendered or not
		// Copied from {@link EntityRenderer#renderHand(float, int)}
		final Minecraft mc = MCWBClient.MC;
		final GameSettings settings = MCWBClient.SETTINGS;
		final Entity entity = mc.getRenderViewEntity();
		if(
			settings.thirdPersonView != 0
			|| (
				entity instanceof EntityLivingBase
				&& ( ( EntityLivingBase ) entity ).isPlayerSleeping()
			)
			|| settings.hideGUI
			|| mc.playerController.isSpectator()
			|| (
				this.mainItem.renderInHand( EnumHand.MAIN_HAND )
				&& this.offItem.renderInHand( EnumHand.OFF_HAND )
			)
		) return true;
		
		// Otherwise, setup orientation for vanilla item rendering
		final Vec3f cameraRot = Vec3f.locate();
		this.cameraController.getCameraRot( cameraRot );
		GL11.glRotatef( cameraRot.z, 0F, 0F, 1F );
		GL11.glRotatef( cameraRot.x, 1F, 0F, 0F );
		GL11.glRotatef( cameraRot.y - this.player.rotationYaw, 0F, 1F, 0F );
		GL11.glRotatef( -this.player.rotationPitch, 1F, 0F, 0F );
		cameraRot.release();
		return false;
	}
	
	public boolean onRenderSpecificHand( EnumHand hand )
	{
		final IInUseItem item = hand == EnumHand.MAIN_HAND ? this.mainItem : this.offItem;
		return item.onRenderSpecificHand( hand );
	}
	
	public void onCameraSetup( CameraSetup evt )
	{
		final Vec3f cameraRot = Vec3f.locate();
		this.cameraController.getCameraRot( cameraRot );
		evt.setYaw( 180F + cameraRot.y );
		evt.setPitch( cameraRot.x );
		evt.setRoll( cameraRot.z );
		cameraRot.release();
	}
	
	public boolean hideCrosshair() { return this.mainItem.hideCrosshair(); }
	
	public boolean onMouseWheelInput( int dWheel ) {
		return this.mainItem.onMouseWheelInput( dWheel );
	}
	
	public void onKeyPress( IKeyBind key ) { this.mainItem.onKeyPress( key ); }
	
	public void onKeyRelease( IKeyBind key ) { this.mainItem.onKeyRelease( key ); }
	
	@Override
	public void trySwapHand()
	{
		if( !this.player.isSpectator() && !this.mainItem.onSwapHand( this.player ) )
			this.sendToServer( new PacketCode( Code.SWAP_HAND ) );
	}
}
