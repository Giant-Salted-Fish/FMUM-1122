package com.fmum.client;

import java.util.Collection;

import org.lwjgl.opengl.GL11;

import com.fmum.client.input.InputHandler;
import com.fmum.client.input.MetaKeyBind;
import com.fmum.client.render.ModelDebugBox;
import com.fmum.common.AutowireLogger;
import com.fmum.common.FMUM;
import com.fmum.common.item.MetaHostItem;
import com.fmum.common.item.MetaItem;
import com.fmum.common.meta.MetaBase;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.event.EntityViewRenderEvent.CameraSetup;
import net.minecraftforge.client.event.EntityViewRenderEvent.FOVModifier;
import net.minecraftforge.client.event.FOVUpdateEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.client.event.RenderSpecificHandEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.RenderTickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly( Side.CLIENT )
@EventBusSubscriber( modid = FMUM.MODID, value = Side.CLIENT )
public abstract class EventHandlerClient
{
	private static final AutowireLogger log = new AutowireLogger() { };
	
	public static float
		camRoll = 0F,
		camYaw = 0F,
		camPitch = 0F;
	
	/**
	 * Partial tick time for smoothing between ticks in rendering
	 */
	public static float renderTickTime = 0F;
	
	private EventHandlerClient() { }
	
	@SubscribeEvent
	public static void onModelRegister( ModelRegistryEvent evt )
	{
		log.log().info( I18n.format( "fmum.onmodelregistration" ) );
		
		final Collection< MetaItem > values = MetaItem.regis.values();
		for( MetaItem meta : values )
			meta.onModelRegister( evt );
		
		log.log().info(
			I18n.format(
				"fmum.modelregistrationcomplete",
				Integer.toString( values.size() )
			)
		);
	}
	
	@SubscribeEvent
	public static void onClientTick( ClientTickEvent evt )
	{
		switch( evt.phase )
		{
		case START:
			break;
		default: FMUMClient.MOD.tick(); // TODO: maybe move tick to common
		}
	}
	
	@SubscribeEvent
	public static void onGameOverlayRender( RenderGameOverlayEvent.Pre evt )
	{
		switch( evt.getType() )
		{
		case CROSSHAIRS:
			evt.setCanceled( MetaHostItem.getMeta(
				FMUMClient.mc.player.inventory.getCurrentItem()
			).disableCrosshair() );
			break;
		default:;
		}
	}
	
	/**
	 * Update partial tick time for after rendering
	 */
	public static void onRenderTick( RenderTickEvent evt ) {
		if( evt.phase == Phase.START ) renderTickTime = evt.renderTickTime;
	}
	
	@SubscribeEvent
	public static void onHandRender( RenderHandEvent evt )
	{
		// Check condition to fall back to original render method
		final Minecraft mc = FMUMClient.mc;
		final GameSettings settings = FMUMClient.settings;
		final Entity renderViewEntity = mc.getRenderViewEntity();
		if(
			settings.thirdPersonView != 0
			|| (
				renderViewEntity instanceof EntityLivingBase
				&& ( ( EntityLivingBase ) renderViewEntity ).isPlayerSleeping()
			)
			|| settings.hideGUI
			|| mc.playerController.isSpectator()
		) return;
		
		// Check if holding one FMUM item
		final EntityPlayerSP player = mc.player;
		ItemStack stack = player.inventory.getCurrentItem();
		Item item = stack.getItem();
		if( !( item instanceof MetaHostItem ) ) return;
		
		MetaItem meta = ( ( MetaHostItem ) item ).meta();
		// TODO: off-hand
		// TODO: call a render method here to render parts that does not interact with light
		
		// Holding one, do customized rendering
		final EntityRenderer entityRenderer = mc.entityRenderer;
		entityRenderer.enableLightmap();
		
		/// Key lighting codes copied from {@link ItemRenderer#renderItemInFirstPerson(float)}
		// {@link ItemRenderer#rotateArroundXAndY(float, float)}
		GL11.glPushMatrix();
		GL11.glRotatef( camPitch, 1F, 0F, 0F ); // player.rotationPitch
		GL11.glRotatef( camYaw, 0F, 1F, 0F );   // player.rotationYaw
		RenderHelper.enableStandardItemLighting();
		GL11.glPopMatrix();

		// {@link ItemRenderer#setLightmap()}
		int light = mc.world.getCombinedLight(
			new BlockPos(
				player.posX,
				player.posY + player.getEyeHeight(),
				player.posZ
			),
			0
		);
		OpenGlHelper.setLightmapTextureCoords(
			OpenGlHelper.lightmapTexUnit,
			light & 65535,
			light >> 16
		);
		
		// {@link ItemRenderer#rotateArm(float)} is not applied to avoid shift
		
		// Re-scale may not needed
		GlStateManager.enableRescaleNormal();
		
		// Outer layer has pushed a matrix, hence not push matrix needed here
//		GlStateManager.disableCull();
		meta.onRenderInHand( stack );
		
		GlStateManager.disableRescaleNormal();
		RenderHelper.disableStandardItemLighting();
		
		// Do not forget to disable light map
		entityRenderer.disableLightmap();
		
		// Cancel event if no native item render in off-hand
		evt.setCanceled( player.inventory.offHandInventory.get( 0 ).isEmpty() );
	}
	
	/**
	 * Specified rendering has been done in {@link #onHandRender(RenderHandEvent)}. But
	 * {@link RenderHandEvent} may not being canceled due to a normal off-hand item. In this case we
	 * just cancel the rendering for {@link FMUM} item.
	 */
	@SubscribeEvent
	public static void onSpecificHandRender( RenderSpecificHandEvent evt ) {
		evt.setCanceled( evt.getItemStack().getItem() instanceof MetaHostItem );
	}
	
	/**
	 * Disable dynamic fov
	 */
	@SubscribeEvent
	public static void onFOVUpdate( FOVUpdateEvent evt ) { evt.setNewfov(1F); }
	
	/**
	 * TODO: Apply fov modification here for scope glass texture rendering
	 */
	@SubscribeEvent
	public static void onFOVModify( FOVModifier evt )
	{
		
	}
	
	/**
	 * Apply camera roll
	 */
	@SubscribeEvent
	public static void onCameraSetup( CameraSetup evt )
	{
//		evt.setRoll( camRoll );
//		evt.setYaw( camYaw );
//		evt.setPitch( camPitch );
	}
	
	@SubscribeEvent
	public static void onPlayerRender(RenderPlayerEvent.Pre evt)
	{
//		GlStateManager.disableCull();
//		FMUMClient.mc.renderEngine.bindTexture(ResourceManager.getTexture("skins/fnmk20ssr-desertyellow.png"));
//		ModelFNMK20SSR.INSTANCE.render();
		ModelDebugBox.INSTANCE.render();
	}
	
	@SubscribeEvent
	public static void onMouseInput( MouseEvent evt )
	{
		int dWheel = evt.getDwheel();
		if( dWheel != 0 ) return;
		
		ItemStack stack = FMUMClient.MOD.prevStack;
		evt.setCanceled( MetaHostItem.getMeta( stack ).onMouseWheelInput( stack, dWheel ) );
	}
	
	/** 
	 * @see InputHandler
	 */
	public static void onInput( InputEvent evt )
	{
		// Avoid key input if yet not enter a world or a GUI is actived
		if( FMUMClient.mc.player == null || FMUMClient.mc.currentScreen != null )
			return;
		
		// Update keys
		for( MetaKeyBind key : InputHandler.globalKeys ) key.update();
		for( MetaKeyBind key : InputHandler.CO.down ? InputHandler.coKeys : InputHandler.incoKeys )
			key.update();
	}
	
	private static boolean modelCompiled = false;
	/**
	 * 3D models will be build client side on the first time of the world load
	 */
	@SubscribeEvent
	public static void onWorldLoad( WorldEvent.Load evt )
	{
		// Avoid model compile on player local server
		if( !evt.getWorld().isRemote || modelCompiled )
			return;
		
		for( MetaBase meta : MetaBase.regis.values() )
			meta.onModelLoad();
		modelCompiled = true;
	}
}
