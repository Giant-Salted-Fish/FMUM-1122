package com.fmum.player;

import com.fmum.FMUM;
import com.fmum.ModConfigClient;
import com.fmum.input.IInput;
import com.fmum.item.IEquippedItem;
import com.fmum.item.IItem;
import gsf.util.math.Vec3f;
import gsf.util.render.GLUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiVideoSettings;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
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
	
	
	/**
	 * Player's eye position.
	 */
	public final Vec3f position = new Vec3f();
	public final Vec3f prev_position = new Vec3f();
	
	public final Vec3f velocity = new Vec3f();
	public final Vec3f prev_velocity = new Vec3f();
	
	public final Vec3f acceleration = new Vec3f();
	public final Vec3f prev_acceleration = new Vec3f();
	
	public IPlayerCamera camera = new PlayerCamera() {
		@Override
		protected float _getMouseSensitivity()
		{
			final GameSettings settings = Minecraft.getMinecraft().gameSettings;
			final IEquippedItem equipped = PlayerPatchClient.this.main_equipped;
			final IItem item = PlayerPatchClient.this.main_item;
			return equipped.getMouseSensitivity( settings.mouseSensitivity, item );
		}
	};
	
	
	PlayerPatchClient() {
		instance = this;
	}
	
	@Override
	void _tick( EntityPlayer player )
	{
		super._tick( player );
		
		// TODO: maybe only update this in GUI event?
		final Minecraft mc = Minecraft.getMinecraft();
		final boolean no_gui_active = mc.currentScreen == null;
		if ( no_gui_active )
		{
			final GameSettings settings = mc.gameSettings;
			final IEquippedItem equipped = this.main_equipped;
			final IItem item = this.main_item;
			settings.viewBobbing = equipped.getViewBobbing( ori_view_bobbing, item );
		}
		
		// Update player motion info.
		this.prev_position.set( this.position );
		this.position.set(
			0.0F +                  ( float ) player.posX,
			player.getEyeHeight() + ( float ) player.posY,
			0.0F +                  ( float ) player.posZ
		);
		
		this.prev_velocity.set( this.velocity );
		this.velocity.set( this.position );
		this.velocity.sub( this.prev_position );
		
		this.prev_acceleration.set( this.acceleration );
		this.acceleration.set( this.velocity );
		this.acceleration.sub( this.prev_velocity );
		
		// Tick camera effects.
		this.camera.tickCamera();
		
		// Ensure mouse helper is properly setup.
		mouse_helper_checker.run();
	}
	
	private void __onMouseXYChange( MouseHelper mouse )
	{
		// This method is called right before the render and player rotation \
		// is also updated. Hence, it is the proper place to fire prepare \
		// render callback.
		this.camera.prepareRender( mouse );
		this.main_equipped.prepareRenderInHand( EnumHand.MAIN_HAND, this.main_item );
		this.off_equipped.prepareRenderInHand( EnumHand.OFF_HAND, this.off_item );
	}
	
	
	/**
	 * There is only  one {@link Minecraft#player} instance, so you can directly
	 * access the patched instance here.
	 */
	public static PlayerPatchClient get() {
		return instance;
	}
	
	@SubscribeEvent
	static void _onRenderGameOverlay$Pre( RenderGameOverlayEvent.Pre evt )
	{
		if ( evt.getType() == ElementType.CROSSHAIRS )
		{
			
			final IEquippedItem equipped = instance.main_equipped;
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
		
		instance.camera.getCameraSetup().glApply();
	}
	
	@SubscribeEvent
	static void _onRenderHand( RenderHandEvent evt )
	{
		// Check if hand should be rendered or not.
		// Copied from {@link EntityRenderer#renderHand(float, int)}.
		final Minecraft mc = Minecraft.getMinecraft();
		final GameSettings settings = mc.gameSettings;
		final Entity entity = mc.getRenderViewEntity();
		final boolean should_cancel_hand_render = (
			settings.thirdPersonView != 0
			|| (
				entity instanceof EntityLivingBase
				&& ( ( EntityLivingBase ) entity ).isPlayerSleeping()
			)
			|| settings.hideGUI
			|| mc.playerController.isSpectator()
			|| (
				instance.main_equipped.renderInHand( EnumHand.MAIN_HAND, instance.main_item )
				&& instance.off_equipped.renderInHand( EnumHand.OFF_HAND, instance.off_item )
			)
		);
		if ( should_cancel_hand_render )
		{
			evt.setCanceled( true );
			return;
		}
		
		/// Otherwise, setup orientation for vanilla item rendering.
		// Setup item lighting orientation.
		instance.camera.getCameraSetup().glApply();
		
		// Cancel vanilla item lighting orientation.
		final EntityPlayerSP player = mc.player;
		GLUtil.glRotateYf( 180.0F - player.rotationYaw );
		GLUtil.glRotateXf( -player.rotationPitch );
	}
	
	@SubscribeEvent
	static void _onRenderSpecificHand( RenderSpecificHandEvent evt )
	{
		final EnumHand hand = evt.getHand();
		final boolean is_main = hand == EnumHand.MAIN_HAND;
		final IEquippedItem equipped = is_main ? instance.main_equipped : instance.off_equipped;
		final IItem item = is_main ? instance.main_item : instance.off_item;
		final boolean cancel = equipped.renderSpecificInHand( hand, item );
		evt.setCanceled( cancel );
	}
	
	@SubscribeEvent
	static void _onMouseInput( MouseEvent evt )
	{
		final int dwheel = evt.getDwheel();
		if ( dwheel != 0 )
		{
			final IEquippedItem equipped = instance.main_equipped;
			final IItem item = instance.main_item;
			final boolean cancel = equipped.onMouseWheelInput( dwheel, item );
			evt.setCanceled( cancel );
		}
	}
	
	public void onInputUpdate( String name, IInput input ) {
		this.main_equipped = this.main_equipped.onInputUpdate( name, input, this.main_item );
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
