package com.fmum.client;

import org.lwjgl.opengl.GL11;

import com.fmum.client.KeyManager.Key;
import com.fmum.client.model.Model;
import com.fmum.client.model.oc.ModelFNMK20SSR;
import com.fmum.common.EventHandler;
import com.fmum.common.EventHandler.RequireItemRegister;
import com.fmum.common.FMUM;
import com.fmum.common.type.ItemInfo;
import com.fmum.common.type.TypeInfo;

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
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.client.event.RenderSpecificHandEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.RenderTickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
@EventBusSubscriber(value = Side.CLIENT, modid = FMUM.MODID)
public abstract class EventHandlerClient
{
	public static float
		renderCamRoll = 0F,
		renderCamYaw = 0F,
		renderCamPitch = 0F;
	
	private EventHandlerClient() { }
	
	@SubscribeEvent
	public static void onModelRegister(ModelRegistryEvent evt)
	{
		FMUM.log.info(I18n.format("fmum.onmodelregistration"));
		
		for(RequireItemRegister rir : EventHandler.itemsWaitForRegistration)
			rir.onModelRegister(evt);
		
		FMUM.log.info(
			I18n.format(
				"fmum.modelregistrationcomplete",
				Integer.toString(EventHandler.itemsWaitForRegistration.size())
			)
		);
		EventHandler.itemsWaitForRegistration.clear();
	}
	
	@SubscribeEvent
	public static void onTick(ClientTickEvent evt)
	{
		switch(evt.phase)
		{
		case START:
			break;
		case END:
			FMUMClient.tick();
		}
	}
	
	@SubscribeEvent
	public static void onGameOverlayRender(RenderGameOverlayEvent evt)
	{
		switch(evt.getType())
		{
		case CROSSHAIRS:
			ItemStack stack = FMUM.mc.player.inventory.getCurrentItem();
			if(
				stack.getItem() instanceof ItemInfo
				&& ((ItemInfo)stack.getItem()).disableCrosshair()
			) evt.setCanceled(true);
			return;
		default:;
		}
	}
	
	/**
	 * Update partial tick time for after later rendering
	 */
	@SubscribeEvent
	public static void onRenderTick(RenderTickEvent evt) {
		if(evt.phase == Phase.START) Model.smoother = evt.renderTickTime;
	}
	
	@SubscribeEvent
	public static void onHandRender(RenderHandEvent evt)
	{
		// Check condition to fall back to original render method
		final Minecraft mc = FMUMClient.mc;
		final GameSettings settings = mc.gameSettings;
		final Entity renderViewEntity = mc.getRenderViewEntity();
		if(
			settings.thirdPersonView != 0
			|| (
				renderViewEntity instanceof EntityLivingBase
				&& ((EntityLivingBase)renderViewEntity).isPlayerSleeping()
			)
			|| settings.hideGUI
			|| mc.playerController.isSpectator()
		) return;
		
		// Check if holding one FMUM item
		final EntityPlayerSP player = mc.player;
		ItemStack stack = player.inventory.getCurrentItem();
		Item item = stack.getItem();
		if(!(item instanceof ItemInfo)) return;
		
		// Holding one, do customized rendering
		final EntityRenderer entityRenderer = mc.entityRenderer;
		entityRenderer.enableLightmap();
		
		/// Key lighting codes copied from {@link ItemRenderer#renderItemInFirstPerson(float)}
		// {@link ItemRenderer#rotateArroundXAndY(float, float)}
		GL11.glPushMatrix();
		GL11.glRotated(renderCamPitch, 1D, 0D, 0D); // player.rotationPitch
		GL11.glRotated(renderCamYaw, 0D, 1D, 0D);   // player.rotationYaw
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
		((ItemInfo)stack.getItem()).renderFP(stack);
		
		GlStateManager.disableRescaleNormal();
		RenderHelper.disableStandardItemLighting();
		
		// Do not forget to disable light map
		entityRenderer.disableLightmap();
		
		// Cancel event if no native item to render in off-hand to render
		if(player.inventory.offHandInventory.get(0).isEmpty())
			evt.setCanceled(true);
	}
	
	/**
	 * Specified rendering has been done in {@link #onHandRender(RenderHandEvent)}. But
	 * {@link RenderHandEvent} may not being canceled due to a normal off-hand item. In this case we
	 * just cancel the rendering for {@link FMUM} item.
	 */
	@SubscribeEvent
	public static void onSpecificHandRender(RenderSpecificHandEvent evt) {
		if(evt.getItemStack().getItem() instanceof ItemInfo) evt.setCanceled(true);
	}
	
	/**
	 * Disable dynamic fov
	 */
	@SubscribeEvent
	public static void onFOVUpdate(FOVUpdateEvent evt) { evt.setNewfov(1F); }
	
	/**
	 * Apply fov modification here for scope glass texture rendering
	 */
	@SubscribeEvent
	public static void onFOVModify(FOVModifier evt)
	{
		// TODO
	}
	
	/**
	 * Apply camera roll
	 */
	@SubscribeEvent
	public static void onCameraSetup(CameraSetup evt)
	{
		evt.setRoll(renderCamRoll);
		evt.setYaw(renderCamYaw);
		evt.setPitch(renderCamPitch);
	}
	
	@SubscribeEvent
	public static void onPlayerRender(RenderPlayerEvent.Pre evt)
	{
//		GlStateManager.disableCull();
		FMUMClient.mc.renderEngine.bindTexture(ResourceManager.getTexture("skins/fnmk20ssr-desertyellow.png"));
//		FMUMClient.mc.renderEngine.bindTexture(ResourceManager.getTexture("skins/test.png"));
//		FMUMClient.mc.renderEngine.bindTexture(ResourceManager.TEXTURE_GREEN);
		ModelFNMK20SSR.INSTANCE.render();
//		ModelTestBox.INSTANCE.render();
	}
	
	/**
	 * @see KeyManager
	 */
	@SubscribeEvent
	public static void onInput(InputEvent evt)
	{
		// Avoid key input if yet not enter a world or a GUI is activated
		if(FMUMClient.mc.player == null || FMUMClient.mc.currentScreen != null) return;
		
		// Update keys upon condition
		for(Key k : KeyManager.primaryKeys) k.update();
		for(Key k : Key.CO.down() ? KeyManager.coKeys : KeyManager.inCoKeys) k.update();
	}
	
	@SubscribeEvent
	public static void onPlayerLogin(PlayerLoggedInEvent evt)
	{
		// TODO: sync config
	}
	
	private static boolean modelCompiled = false;
	/**
	 * 3D models will be built client side on world load if have not built
	 */
	@SubscribeEvent
	public static void onWorldLoad(WorldEvent.Load evt)
	{
		// Avoid model compile on player local server
		if(!evt.getWorld().isRemote || modelCompiled) return;
		
		for(TypeInfo t : TypeInfo.types.values())
			t.loadModel();
		modelCompiled = true;
	}
}
