package com.fmum.player;

import com.fmum.FMUM;
import com.fmum.ModConfigClient;
import com.fmum.input.InputUpdateEvent;
import com.fmum.item.IItem;
import com.fmum.item.IMainEquipped;
import gsf.util.lang.Type;
import gsf.util.math.Vec3f;
import gsf.util.render.GLUtil;
import gsf.util.render.IPose;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiVideoSettings;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumHand;
import net.minecraft.util.MouseHelper;
import net.minecraftforge.client.event.EntityViewRenderEvent.CameraSetup;
import net.minecraftforge.client.event.FOVUpdateEvent;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.client.event.RenderSpecificHandEvent;
import net.minecraftforge.fml.client.event.ConfigChangedEvent.PostConfigChangedEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly( Side.CLIENT )
@EventBusSubscriber( modid = FMUM.MODID, value = Side.CLIENT )
public final class PlayerPatchClient extends PlayerPatch
{
	private static PlayerPatchClient instance;
	
	
	// >>> Start MouseHelper Stuffs <<<
	private static final MouseHelper MOUSE_HELPER = new MouseHelper() {
		@Override
		public void mouseXYChange()
		{
			super.mouseXYChange();
			instance.__onMouseXYChange( this );
		}
	};
	
	private static final Runnable DEFAULT_CHECKER = () -> {
		final Minecraft mc = Minecraft.getMinecraft();
		mc.mouseHelper = MOUSE_HELPER;
	};
	
	private static final Runnable COMPATIBLE_CHECKER = () -> {
		final Minecraft mc = Minecraft.getMinecraft();
		final MouseHelper mouse_helper = mc.mouseHelper;
		final Class< ? > helper_class = mouse_helper.getClass();
		final boolean is_default_helper = mouse_helper == MOUSE_HELPER;
		final boolean is_compatible_helper = helper_class == FlanCompatibleMouseHelper.class;
		if ( is_default_helper || is_compatible_helper ) {
			return;
		}
		
		final boolean is_vanilla_helper = helper_class == MouseHelper.class;
		mc.mouseHelper = is_vanilla_helper ? MOUSE_HELPER : new FlanCompatibleMouseHelper( mouse_helper );
	};
	
	private static Runnable mouse_helper_checker;
	static
	{
		// TODO: Check and make sure this is called on the right time.
		final boolean flag = ModConfigClient.use_flan_compatible_mousehelper;
		mouse_helper_checker = flag ? COMPATIBLE_CHECKER : DEFAULT_CHECKER;
	}
	
	@SubscribeEvent
	static void _onPostConfigChanged( PostConfigChangedEvent evt )
	{
		if ( evt.getModID().equals( FMUM.MODID ) )
		{
			final boolean flag = ModConfigClient.use_flan_compatible_mousehelper;
			mouse_helper_checker = flag ? COMPATIBLE_CHECKER : DEFAULT_CHECKER;
		}
	}
	
	private final static class FlanCompatibleMouseHelper extends MouseHelper
	{
		private final MouseHelper wrapped;
		
		private FlanCompatibleMouseHelper( MouseHelper wrapped ) {
			this.wrapped = wrapped;
		}
		
		@Override
		public void mouseXYChange()
		{
			this.wrapped.mouseXYChange();
			instance.__onMouseXYChange( this.wrapped );
		}
		
		@Override
		public void grabMouseCursor() {
			this.wrapped.grabMouseCursor();
		}
		
		@Override
		public void ungrabMouseCursor() {
			this.wrapped.ungrabMouseCursor();
		}
		
		@Override
		@SuppressWarnings( { "EqualsWhichDoesntCheckParameterClass", "EqualsDoesntCheckParameterClass" } )
		public boolean equals( Object obj ) {
			return this.wrapped.equals( obj );
		}
	}
	// >>> End MouseHelper Part <<<
	
	
	private static boolean ori_view_bobbing = Minecraft.getMinecraft().gameSettings.viewBobbing;
	
	
	private final CameraController camera_controller = new CameraController();
	private IPose camera_setup = IPose.EMPTY;
	
	/**
	 * Player's eye position.
	 */
	public final Vec3f position = new Vec3f();
	public final Vec3f prev_position = new Vec3f();
	
	public final Vec3f velocity = new Vec3f();
	public final Vec3f prev_velocity = new Vec3f();
	
	public final Vec3f acceleration = new Vec3f();
	public final Vec3f prev_acceleration = new Vec3f();
	
	
	PlayerPatchClient() {
		instance = this;
	}
	
	@Override
	void _tick( EntityPlayer player )
	{
		this.camera_controller.tickCamera();
		
		// TODO: maybe only update this in GUI event?
		final Minecraft mc = Minecraft.getMinecraft();
		final boolean no_gui_active = mc.currentScreen == null;
		if ( no_gui_active )
		{
			final GameSettings settings = mc.gameSettings;
			final IMainEquipped equipped = this.main_equipped;
			final IItem item = this.main_item;
			settings.viewBobbing = equipped.getViewBobbing( ori_view_bobbing, item );
		}
		
		// Update player motion info.
		this.prev_position.set( this.position );
		this.position.set(
			( float ) player.posX,
			( float ) player.posY + player.getEyeHeight(),
			( float ) player.posZ
		);
		
		this.prev_velocity.set( this.velocity );
		this.velocity.set( this.position );
		this.velocity.sub( this.prev_position );
		
		this.prev_acceleration.set( this.acceleration );
		this.acceleration.set( this.velocity );
		this.acceleration.sub( this.prev_velocity );
		
		// Ensure mouse helper is properly setup.
		mouse_helper_checker.run();
		
		super._tick( player );
	}
	
	private void __onMouseXYChange( MouseHelper mouse )
	{
		// This method is called right before the render and player rotation \
		// is also updated. Hence, it is the proper place to fire prepare \
		// render callback.
		this.camera_setup = this.camera_controller.prepareRender( mouse );
		this.main_equipped.prepareRenderInHand( this.main_item );
		this.off_equipped.prepareRenderInHand( this.off_item );
	}
	
	public IPose getCameraSetup() {
		return this.camera_setup;
	}
	
	
	/**
	 * There is only  one {@link Minecraft#player} instance, so you can directly
	 * access the patched instance here.
	 */
	public static PlayerPatchClient get() {
		return instance;
	}
	
	static float _getMouseSensitivity()
	{
		final GameSettings settings = Minecraft.getMinecraft().gameSettings;
		final IMainEquipped equipped = instance.main_equipped;
		final IItem item = instance.main_item;
		return equipped.getMouseSensitivity( settings.mouseSensitivity, item );
	}
	
	@SubscribeEvent
	static void _onRenderGameOverlay$Pre( RenderGameOverlayEvent.Pre evt )
	{
		if ( evt.getType() == ElementType.CROSSHAIRS )
		{
			final IMainEquipped equipped = instance.main_equipped;
			final IItem item = instance.main_item;
			evt.setCanceled( equipped.shouldDisableCrosshair( item ) );
		}
	}
	
	@SubscribeEvent
	static void _onCameraSetup( CameraSetup evt )
	{
		evt.setYaw( 0.0F );
		evt.setPitch( 0.0F );
		evt.setRoll( 0.0F );
		
		instance.camera_setup.glApply();
	}
	
	@SubscribeEvent
	static void _onRenderHand( RenderHandEvent evt )
	{
		// Check if hand should be rendered or not.
		// Copied from {@link EntityRenderer#renderHand(float, int)}.
		final Minecraft mc = Minecraft.getMinecraft();
		final GameSettings settings = mc.gameSettings;
		final Entity entity = mc.getRenderViewEntity();
		final boolean skip_hand_render = (
			settings.thirdPersonView != 0
			|| Type.cast( entity, EntityPlayerSP.class ).map( EntityPlayer::isPlayerSleeping ).orElse( false )
			|| settings.hideGUI
			|| mc.playerController.isSpectator()
		);
		if ( skip_hand_render ) {
			return;
		}
		
		final boolean is_custom_main = instance.main_equipped.renderInHand( instance.main_item );
		final boolean is_custom_off = instance.off_equipped.renderInHand( instance.off_item );
		if ( is_custom_main && is_custom_off ) {
			evt.setCanceled( true );
		}
		else
		{
			// Otherwise, setup orientation for vanilla item rendering.
			// Setup item lighting orientation.
			instance.camera_setup.glApply();
			
			// Cancel vanilla item lighting orientation.
			final EntityPlayerSP player = mc.player;
			GLUtil.glRotateYf( 180.0F - player.rotationYaw );
			GLUtil.glRotateXf( -player.rotationPitch );
		}
	}
	
	@SubscribeEvent
	static void _onRenderSpecificHand( RenderSpecificHandEvent evt )
	{
		final boolean cancel = (
			evt.getHand() == EnumHand.MAIN_HAND
			? instance.main_equipped.renderSpecificInHand( instance.main_item )
			: instance.off_equipped.renderSpecificInHand( instance.off_item )
		);
		evt.setCanceled( cancel );
	}
	
	@SubscribeEvent
	static void _onMouseInput( MouseEvent evt )
	{
		final int dwheel = evt.getDwheel();
		if ( dwheel != 0 )
		{
			final IMainEquipped equipped = instance.main_equipped;
			final IItem item = instance.main_item;
			final boolean cancel = equipped.onMouseWheelInput( dwheel, item );
			evt.setCanceled( cancel );
		}
	}
	
	@SubscribeEvent
	static void _onInputUpdate( InputUpdateEvent evt )
	{
		final IItem item = instance.main_item;
		final IMainEquipped eq = instance.main_equipped;
		instance.main_equipped = eq.onInputUpdate( evt.name, evt.input, item );
	}
	
	@SubscribeEvent
	static void _onGUIChange( GuiOpenEvent evt )
	{
		final Minecraft mc = Minecraft.getMinecraft();
		final GuiScreen gui = evt.getGui();
		final GuiScreen prev_gui = mc.currentScreen;
		final GameSettings settings = mc.gameSettings;
		
		// TODO: This may also be accomplished by replacing the GUI.
		if ( gui instanceof GuiVideoSettings )
		{
			settings.viewBobbing = ori_view_bobbing;
			// TODO: gamma setting
		}
		else if ( prev_gui instanceof GuiVideoSettings )
		{
			ori_view_bobbing = settings.viewBobbing;
		}
	}
	
	// TODO: Apply FOV modification here for scope lens texture rendering.
//	@SubscribeEvent
//	static void _onFOVModify( FOVModifier evt )
//	{
//
//	}
	
	// Disable dynamic FOV.
	@SubscribeEvent
	static void _onFOVUpdate( FOVUpdateEvent evt ) {
		evt.setNewfov( 1.0F );
	}
}
