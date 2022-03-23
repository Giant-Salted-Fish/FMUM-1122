package com.fmum.client;

import org.lwjgl.util.glu.Project;

import com.fmum.client.KeyManager.Key;
import com.fmum.common.EventHandler;
import com.fmum.common.FMUM;
import com.fmum.common.type.ItemInfo;
import com.fmum.common.type.TypeInfo;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.client.event.RenderSpecificHandEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
@EventBusSubscriber(value = Side.CLIENT, modid = FMUM.MODID)
public abstract class EventHandlerClient extends EventHandler
{
	@SubscribeEvent
	public static void onModelRegister(ModelRegistryEvent evt)
	{
		FMUM.log.info(I18n.format("fmum.onmodelregistration"));
		
		for(RequireItemRegister rir : itemsWaitForRegistration)
			rir.onModelRegister(evt);
		
		FMUM.log.info(
			I18n.format(
				"fmum.modelregistrationcomplete",
				Integer.toString(itemsWaitForRegistration.size())
			)
		);
		itemsWaitForRegistration.clear();
	}
	
	private static final Minecraft mc = Minecraft.getMinecraft();
	@SubscribeEvent
	public static void renderHeldItem(RenderSpecificHandEvent event)
	{
		ItemStack stack = event.getItemStack();
		if(stack.getItem() instanceof ItemInfo)
		{
			TypeInfo type = ((ItemInfo)stack.getItem()).getType();
			// Cancel the hand render event so that we can do our own.
			event.setCanceled(true);
			
			float partialTicks = event.getPartialTicks();
			EntityRenderer renderer = mc.entityRenderer;
			float farPlaneDistance = mc.gameSettings.renderDistanceChunks * 16F;
			ItemRenderer itemRenderer = mc.getItemRenderer();
			//Unknown function. But definitely messes up the render pipeline, causing other mods and shaders to break
			//GlStateManager.clear(256);
			GlStateManager.matrixMode(5889);
			GlStateManager.loadIdentity();
			
			Project.gluPerspective(getFOVModifier(partialTicks), (float)mc.displayWidth / (float)mc.displayHeight,
					0.05F, farPlaneDistance * 2.0F);
			GlStateManager.matrixMode(5888);
			GlStateManager.loadIdentity();
			
			GlStateManager.pushMatrix();
			hurtCameraEffect(partialTicks);
			
			if(mc.gameSettings.viewBobbing)
				setupViewBobbing(partialTicks);
			
			boolean flag = mc.getRenderViewEntity() instanceof EntityLivingBase &&
					((EntityLivingBase)mc.getRenderViewEntity()).isPlayerSleeping();
			
			if(mc.gameSettings.thirdPersonView == 0
					&& !flag
					&& !mc.gameSettings.hideGUI
					&& !mc.playerController.isSpectator())
			{
				renderer.enableLightmap();
				float f1 = 1.0F;// - (prevEquippedProgress + (equippedProgress - prevEquippedProgress) * partialTicks);
				EntityPlayerSP entityplayersp = mc.player;
				float f2 = entityplayersp.getSwingProgress(partialTicks);
				float f3 = entityplayersp.prevRotationPitch +
						(entityplayersp.rotationPitch - entityplayersp.prevRotationPitch) * partialTicks;
				float f4 = entityplayersp.prevRotationYaw +
						(entityplayersp.rotationYaw - entityplayersp.prevRotationYaw) * partialTicks;
				
				// Setup lighting
				GlStateManager.disableLighting();
				GlStateManager.pushMatrix();
				GlStateManager.rotate(f3, 1.0F, 0.0F, 0.0F);
				GlStateManager.rotate(f4, 0.0F, 1.0F, 0.0F);
				RenderHelper.enableStandardItemLighting();
				GlStateManager.popMatrix();
				
				// Do lighting
				int i = mc.world.getCombinedLight(new BlockPos(entityplayersp.posX,
						entityplayersp.posY + (double)entityplayersp.getEyeHeight(), entityplayersp.posZ), 0);
				OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, (float)(i & 65535),
						(float)(i >> 16));
				
				// Do hand rotations
				float f5 = entityplayersp.prevRenderArmPitch +
						(entityplayersp.renderArmPitch - entityplayersp.prevRenderArmPitch) * partialTicks;
				float f6 = entityplayersp.prevRenderArmYaw +
						(entityplayersp.renderArmYaw - entityplayersp.prevRenderArmYaw) * partialTicks;
				GlStateManager.rotate((entityplayersp.rotationPitch - f5) * 0.1F, 1.0F, 0.0F, 0.0F);
				GlStateManager.rotate((entityplayersp.rotationYaw - f6) * 0.1F, 0.0F, 1.0F, 0.0F);
				
				GlStateManager.enableRescaleNormal();
				GlStateManager.pushMatrix();
				
				// Do vanilla weapon swing
				float f7 = -0.4F * MathHelper.sin(MathHelper.sqrt(f2) * (float)Math.PI);
				float f8 = 0.2F * MathHelper.sin(MathHelper.sqrt(f2) * (float)Math.PI * 2.0F);
				float f9 = -0.2F * MathHelper.sin(f2 * (float)Math.PI);
				GlStateManager.translate(f7, f8, f9);
				
				GlStateManager.translate(0.56F, -0.52F, -0.71999997F);
				GlStateManager.translate(0.0F, f1 * -0.6F, 0.0F);
				GlStateManager.rotate(45.0F, 0.0F, 1.0F, 0.0F);
				float f10 = MathHelper.sin(f2 * f2 * (float)Math.PI);
				float f11 = MathHelper.sin(MathHelper.sqrt(f2) * (float)Math.PI);
				GlStateManager.rotate(f10 * -20.0F, 0.0F, 1.0F, 0.0F);
				GlStateManager.rotate(f11 * -20.0F, 0.0F, 0.0F, 1.0F);
				GlStateManager.rotate(f11 * -80.0F, 1.0F, 0.0F, 0.0F);
				GlStateManager.scale(0.4F, 0.4F, 0.4F);
				
				type.model.render();
				
				GlStateManager.popMatrix();
				GlStateManager.disableRescaleNormal();
				RenderHelper.disableStandardItemLighting();
				renderer.disableLightmap();
			}
			
			GlStateManager.popMatrix();
			
			if(mc.gameSettings.thirdPersonView == 0 && !flag)
			{
				itemRenderer.renderOverlays(partialTicks);
				hurtCameraEffect(partialTicks);
			}
			
			if(mc.gameSettings.viewBobbing)
			{
				setupViewBobbing(partialTicks);
			}
		}
	}
	
	private static void hurtCameraEffect(float partialTicks)
	{
		if(mc.getRenderViewEntity() instanceof EntityLivingBase)
		{
			EntityLivingBase entitylivingbase = (EntityLivingBase)mc.getRenderViewEntity();
			float f1 = (float)entitylivingbase.hurtTime - partialTicks;
			float f2;
			
			if(entitylivingbase.getHealth() <= 0.0F)
			{
				f2 = (float)entitylivingbase.deathTime + partialTicks;
				GlStateManager.rotate(40.0F - 8000.0F / (f2 + 200.0F), 0.0F, 0.0F, 1.0F);
			}
			
			if(f1 < 0.0F)
			{
				return;
			}
			
			f1 /= (float)entitylivingbase.maxHurtTime;
			f1 = MathHelper.sin(f1 * f1 * f1 * f1 * (float)Math.PI);
			f2 = entitylivingbase.attackedAtYaw;
			GlStateManager.rotate(-f2, 0.0F, 1.0F, 0.0F);
			GlStateManager.rotate(-f1 * 14.0F, 0.0F, 0.0F, 1.0F);
			GlStateManager.rotate(f2, 0.0F, 1.0F, 0.0F);
		}
	}
	
	private static void setupViewBobbing(float partialTicks)
	{
		if(mc.getRenderViewEntity() instanceof EntityPlayer)
		{
			EntityPlayer entityplayer = (EntityPlayer)mc.getRenderViewEntity();
			float f1 = entityplayer.distanceWalkedModified - entityplayer.prevDistanceWalkedModified;
			float f2 = -(entityplayer.distanceWalkedModified + f1 * partialTicks);
			float f3 =
					entityplayer.prevCameraYaw + (entityplayer.cameraYaw - entityplayer.prevCameraYaw) * partialTicks;
			float f4 = entityplayer.prevCameraPitch +
					(entityplayer.cameraPitch - entityplayer.prevCameraPitch) * partialTicks;
			GlStateManager.translate(MathHelper.sin(f2 * (float)Math.PI) * f3 * 0.5F,
					-Math.abs(MathHelper.cos(f2 * (float)Math.PI) * f3), 0.0F);
			GlStateManager.rotate(MathHelper.sin(f2 * (float)Math.PI) * f3 * 3.0F, 0.0F, 0.0F, 1.0F);
			GlStateManager.rotate(Math.abs(MathHelper.cos(f2 * (float)Math.PI - 0.2F) * f3) * 5.0F, 1.0F, 0.0F, 0.0F);
			GlStateManager.rotate(f4, 1.0F, 0.0F, 0.0F);
		}
	}
	
	private static float getFOVModifier(float partialTicks)
	{
		Entity entity = mc.getRenderViewEntity();
		float f1 = 70.0F;
		
		if(entity instanceof EntityLivingBase && ((EntityLivingBase)entity).getHealth() <= 0.0F)
		{
			float f2 = (float)((EntityLivingBase)entity).deathTime + partialTicks;
			f1 /= (1.0F - 500.0F / (f2 + 500.0F)) * 2.0F + 1.0F;
		}
		
		IBlockState state = ActiveRenderInfo.getBlockStateAtEntityViewpoint(mc.world, entity, partialTicks);
		
		if(state.getMaterial() == Material.WATER)
			f1 = f1 * 60.0F / 70.0F;
		
		return f1;
	}
	
	@SubscribeEvent
	public static void onHandRender(RenderHandEvent evt)
	{
	}
	
	/**
	 * @see KeyManager
	 */
	@SubscribeEvent
	public static void onInput(InputEvent evt)
	{
		// Avoid key input if yet not enter a world or a GUI is activated
//		if(FMUM.mc.player == null || FMUM.mc.currentScreen != null) return;
		
		// Update keys upon condition
		for(Key k : KeyManager.primaryKeys) k.update();
		for(Key k : Key.CO.down() ? KeyManager.coKeys : KeyManager.inCoKeys) k.update();
	}
	
	@SubscribeEvent
	public static void onPlayerLogin(PlayerLoggedInEvent evt)
	{
		if(evt != null);
	}
	
	private static boolean modelCompiled = false;
	/**
	 * 3D models will be compile client side on world load if have not compiled
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
	
//	@SubscribeEvent
//	public static void onPlayerRender(RenderPlayerEvent.Pre evt)
//	{
//		evt.setCanceled(true);
//	}
}
