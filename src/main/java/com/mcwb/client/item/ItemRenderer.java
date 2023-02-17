package com.mcwb.client.item;

import org.lwjgl.opengl.GL11;

import com.google.gson.annotations.SerializedName;
import com.mcwb.client.IAutowireBindTexture;
import com.mcwb.client.IAutowireSmoother;
import com.mcwb.client.MCWBClient;
import com.mcwb.client.player.PlayerPatchClient;
import com.mcwb.client.render.IRenderer;
import com.mcwb.client.render.Renderer;
import com.mcwb.common.item.IItem;
import com.mcwb.util.Vec3f;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly( Side.CLIENT )
public class ItemRenderer< T extends IItem > extends Renderer
	implements IItemRenderer< T >, IAutowireBindTexture, IAutowireSmoother
{
	public static final ResourceLocation
		TEXTURE_STEVE = new ResourceLocation( "textures/entity/steve.png" );
	
	public static final ResourceLocation
		TEXTURE_ALEX = new ResourceLocation( "textures/entity/alex.png" );
	
	protected static final IRenderer
		STEVE_ARM = MCWBClient.MOD.loadRenderer( "models/steve_arm.json", "" );
	
	protected static final IRenderer
		ALEX_ARM = MCWBClient.MOD.loadRenderer( "models/alex_arm.json", "" );
	
	protected static final Vec3f HOLD_POS = new Vec3f( -40F / 160F, -50F / 160F, 100F / 160F );
	protected static final Vec3f HOLD_ROT = new Vec3f();
	
	@SerializedName( value = "holdPos", alternate = "pos" )
	protected Vec3f holdPos = HOLD_POS;
	
	@SerializedName( value = "holdRot", alternate = "rot" )
	protected Vec3f holdRot = HOLD_ROT;
	
	@Override
	public void tickInHand( T contexted, EnumHand hand )
	{
		final ItemAnimatorState state = this.animator( hand );
		
		// Simply set position and rotation then update
		state.holdPos.tarPos.set( this.holdPos );
		state.holdPos.update();
		state.holdRot.tarPos.set( this.holdRot );
		state.holdRot.update();
	}
	
	/**
	 * Do not modify this method unless you understand what it does. If you need to do a customized
	 * rendering then override {@link #doRenderInHand()} as your first choice.
	 */
	@Override
	public boolean renderInHand( T contexted, EnumHand hand )
	{
		GL11.glPushMatrix(); {
		
		// Do customized rendering
		final Minecraft mc = MCWBClient.MC;
		final EntityPlayerSP player = mc.player;
		
		// Copied from {@link EntityRenderer#renderHand(float, int)}
		final EntityRenderer renderer = mc.entityRenderer;
		renderer.enableLightmap();
		
		/// Copied from {@link ItemRenderer#renderItemInFirstPerson(float)} ///
		// {@link ItemRenderer#rotateArroundXAndY(float, float)}
		final Vec3f camRot = PlayerPatchClient.instance.camRot;
		GL11.glRotatef( camRot.z, 0F, 0F, 1F );
		GL11.glRotatef( camRot.x, 1F, 0F, 0F );
		GL11.glRotatef( camRot.y, 0F, 1F, 0F );
		RenderHelper.enableStandardItemLighting();
		
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
			light & 0xFFFF, light >> 16
		);
		
		// {@link ItemRenderer#rotateArm(float)} is not applied to avoid shift
		
		// TODO: Re-scale may not needed. Do not forget that there is a disable pair call.
		GlStateManager.enableRescaleNormal();
		
		// Setup and render!
		GL11.glRotatef( 180F - player.rotationYaw, 0F, 1F, 0F );
		GL11.glRotatef( player.rotationPitch, 1F, 0F, 0F );
		this.doRenderInHand( contexted, hand );
		
		GlStateManager.disableRescaleNormal();
		RenderHelper.disableStandardItemLighting();
		/// End of {@link ItemRenderer#renderItemInFirstPerson(float)} ///
		
		renderer.disableLightmap();
		
		} GL11.glPopMatrix();
		return true;
	}
	
	@Override
	public boolean onRenderSpecificHand( T contexted, EnumHand hand ) { return true; }
	
	@Override
	public void render( T contexted )
	{
		this.bindTexture( contexted.texture() );
		this.render();
	}
	
	protected void doRenderInHand( T contexted, EnumHand hand )
	{
		final ItemAnimatorState state = this.animator( hand );
		state.getChannel( ItemAnimatorState.CHANNEL_ITEM, this.smoother(), state.m0 );
		
		glMultMatrix( state.m0 );
		this.render( contexted );
	}
	
	protected ItemAnimatorState animator( EnumHand hand ) { return ItemAnimatorState.INSTANCE; }
}
