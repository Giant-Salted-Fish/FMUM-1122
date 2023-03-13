package com.mcwb.client.item;

import org.lwjgl.opengl.GL11;

import com.mcwb.client.IAutowireBindTexture;
import com.mcwb.client.MCWBClient;
import com.mcwb.client.player.PlayerPatchClient;
import com.mcwb.client.render.IAnimator;
import com.mcwb.client.render.IRenderer;
import com.mcwb.client.render.Renderer;
import com.mcwb.common.item.IEquippedItem;
import com.mcwb.common.item.IItem;
import com.mcwb.util.Mat4f;
import com.mcwb.util.Quat4f;
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

// TODO: EquippedRenderer?
@SideOnly( Side.CLIENT )
public abstract class ItemRenderer< C extends IItem, E extends IEquippedItem< ? extends C > >
	extends Renderer implements IItemRenderer< C, E >, IAutowireBindTexture
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
	
	// TODO: move these things to gun
	protected float holdPosForceMult = 0.25F;
	protected float holdPosMaxForce = 0.125F;
	protected float holdPosDampingFactor = 0.4F;
	
	protected float holdRotForceMult = 1F;
	protected float holdRotMaxForce = 4.25F;
	protected float holdRotDampingFactor = 0.4F;
	
//	@Override
//	public IEquippedItemRenderer< E > onTakeOut( EnumHand hand ) {
//		return this.new EquippedItemRenderer();
//	}
	
	protected abstract class EquippedItemRenderer implements IEquippedItemRenderer< E >, IAnimator
	{
		@Override
		public void getPos( String channel, Vec3f dst )
		{
			switch( channel )
			{
			case CHANNEL_ITEM:
				dst.set( ItemRenderer.this.holdPos );
				break;
				
			default: dst.setZero();
			}
		}
		
		@Override
		public void getRot( String channel, Quat4f dst )
		{
			switch( channel )
			{
			case CHANNEL_ITEM:
				dst.set( ItemRenderer.this.holdRot );
				break;
				
			default: dst.clearRot();
			}
		}
		
//		@Override
//		public void tickInHand( E equipped, EnumHand hand ) { }
//		
//		@Override
//		public void prepareRenderInHandSP( E equipped, EnumHand hand ) { }
		
		@Override
		public boolean renderInHandSP( E equipped, EnumHand hand )
		{
			GL11.glPushMatrix();
			
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
			final double eyeY = player.posY + player.getEyeHeight();
			final BlockPos blockPos = new BlockPos( player.posX, eyeY, player.posZ );
			int light = mc.world.getCombinedLight( blockPos, 0 );
			
			final float x = light & 0xFFFF;
			final float y = light >> 16;
			OpenGlHelper.setLightmapTextureCoords( OpenGlHelper.lightmapTexUnit, x, y );
			
			// {@link ItemRenderer#rotateArm(float)} is not applied to avoid shift
			
			// TODO: Re-scale may not needed. Do not forget that there is a disable pair call.
			GlStateManager.enableRescaleNormal();
			
			// Setup and render!
			GL11.glRotatef( 180F - player.rotationYaw, 0F, 1F, 0F );
			GL11.glRotatef( player.rotationPitch, 1F, 0F, 0F );
			this.doRenderInHandSP( equipped, hand );
			
			GlStateManager.disableRescaleNormal();
			RenderHelper.disableStandardItemLighting();
			/// End of {@link ItemRenderer#renderItemInFirstPerson(float)} ///
			
			renderer.disableLightmap();
			
			GL11.glPopMatrix();
			return true;
		}
		
		@Override
		public boolean onRenderSpecificHandSP( E equipped, EnumHand hand ) { return true; }
		
		@Override
		public IAnimator animator() { return this; }
		
		protected void doRenderInHandSP( E equipped, EnumHand hand )
		{
			final Mat4f mat = Mat4f.locate();
			IAnimator.getChannel( equipped.animator(), CHANNEL_ITEM, mat );
			glMultMatrix( mat );
			mat.release();
			
			ItemRenderer.this.render( equipped.item() );
		}
	}
	
	@Override
	public void render( C contexted )
	{
		this.bindTexture( contexted.texture() );
		this.render();
	}
}
