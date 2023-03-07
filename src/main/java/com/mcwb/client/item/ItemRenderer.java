package com.mcwb.client.item;

import org.lwjgl.opengl.GL11;

import com.mcwb.client.IAutowireBindTexture;
import com.mcwb.client.IAutowireSmoother;
import com.mcwb.client.MCWBClient;
import com.mcwb.client.player.PlayerPatchClient;
import com.mcwb.client.render.IAnimator;
import com.mcwb.client.render.IRenderer;
import com.mcwb.client.render.Renderer;
import com.mcwb.common.item.IItem;
import com.mcwb.util.Mat4f;
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
		STEVE_ARM = MCWBClient.MOD.loadRenderer( "renderers/steve_arm.json", "" );
	
	protected static final IRenderer
		ALEX_ARM = MCWBClient.MOD.loadRenderer( "renderers/alex_arm.json", "" );
	
	private static final Vec3f HOLD_POS = new Vec3f( -40F / 160F, -50 / 160F, 100F / 160F );
	
	protected Vec3f holdPos = HOLD_POS;
	protected Vec3f holdRot = Vec3f.ORIGIN;
	
	protected float holdPosForceMult = 0.25F;
	protected float holdPosMaxForce = 0.125F;
	protected float holdPosDampingFactor = 0.4F;
	
	protected float holdRotForceMult = 1F;
	protected float holdRotMaxForce = 4.25F;
	protected float holdRotDampingFactor = 0.4F;
	
	@Override
	public void tickInHand( T contexted, EnumHand hand )
	{
		final ItemAnimatorState state = this.animator( hand );
		
		// Simply set position and rotation then update
		state.holdPos.tarPos.set( this.holdPos );
		state.holdPos.update(
			this.holdPosForceMult,
			this.holdPosMaxForce,
			this.holdPosDampingFactor
		);
		state.holdRot.tarPos.set( this.holdRot );
		state.holdRot.update(
			this.holdRotForceMult,
			this.holdRotMaxForce,
			this.holdRotDampingFactor
		);
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
		final Vec3f cameraRot = Vec3f.locate();
		PlayerPatchClient.instance.cameraController.getCameraRot( cameraRot );
		GL11.glRotatef( cameraRot.z, 0F, 0F, 1F );
		GL11.glRotatef( cameraRot.x, 1F, 0F, 0F );
		GL11.glRotatef( cameraRot.y, 0F, 1F, 0F );
		RenderHelper.enableStandardItemLighting();
		cameraRot.release();
		
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
		final Mat4f mat = Mat4f.locate();
		final ItemAnimatorState state = this.animator( hand );
		IAnimator.getChannel( state, CHANNEL_ITEM, this.smoother(), mat );
		glMultMatrix( mat );
		mat.release();
		
		this.render( contexted );
	}
	
	protected ItemAnimatorState animator( EnumHand hand ) { return ItemAnimatorState.INSTANCE; }
}
