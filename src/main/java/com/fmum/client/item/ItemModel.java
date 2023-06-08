package com.fmum.client.item;

import com.fmum.client.FMUMClient;
import com.fmum.client.player.PlayerPatchClient;
import com.fmum.client.render.IAnimator;
import com.fmum.client.render.Model;
import com.fmum.common.item.IEquippedItem;
import com.fmum.common.item.IItem;
import com.fmum.util.Mat4f;
import com.fmum.util.Quat4f;
import com.fmum.util.Vec3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

@SideOnly( Side.CLIENT )
public abstract class ItemModel<
	C extends IItem,
	E extends IEquippedItem< ? extends C >,
	R extends IItemRenderer< ? super C, ? extends IEquippedItemRenderer< ? super E > >
> extends Model implements IItemModel< R >
{
	// TODO: these channels seems no need to expose
	public static final String CHANNEL_ITEM = "__item__";
	
	public static final ResourceLocation
		TEXTURE_STEVE = new ResourceLocation( "textures/entity/steve.png" );
	
	public static final ResourceLocation
		TEXTURE_ALEX = new ResourceLocation( "textures/entity/alex.png" );
	
	protected static final Model
		STEVE_ARM = new Model( "models/steve_arm.obj", 0.0625F, true );
	
	protected static final Model
		ALEX_ARM = new Model( "models/alex_arm.obj", 0.0625F, true );
	
	private static final Vec3f HOLD_POS = new Vec3f( -40F / 160F, -50 / 160F, 100F / 160F );
	
	protected Vec3f holdPos = HOLD_POS;
	protected Vec3f holdRot = Vec3f.ORIGIN;
	
	/**
	 * Channel of animation that will be applied to this item.
	 */
	protected String animationChannel = "item";
	
	protected class EquippedItemRenderer implements IEquippedItemRenderer< E >, IAnimator
	{
		protected IAnimator animation = IAnimator.NONE;
		
		protected final Vec3f pos = new Vec3f();
		protected final Quat4f rot = new Quat4f();
		
		// Not protected due to the visibility problem in AmmoModel.
		public EquippedItemRenderer() { }
		
		@Override
		public void useOperateAnimation( IAnimator animation ) { this.animation = animation; }
		
		@Override
		public void update()
		{
			this.updatePosRot(); // This goes ahead of animation update for ModifyAnimation.
			
			// Update animation.
			this.animation.update();
			
			// Apply animation.
			final Mat4f mat = Mat4f.locate();
			mat.setIdentity();
			mat.translate( this.pos );
			mat.rotate( this.rot );
			this.applyChannel( ItemModel.this.animationChannel, mat );
			// TODO: equipped#animator() actually should be #this
			
			mat.get( this.pos );
			this.rot.set( mat );
			mat.release();
		}
		
		@Override
		public void getPos( String channel, Vec3f dst )
		{
			switch ( channel )
			{
			case CHANNEL_ITEM:
				dst.set( this.pos );
				break;
				
			default: this.animation.getPos( channel, dst );
			}
		}
		
		@Override
		public void getRot( String channel, Quat4f dst )
		{
			switch ( channel )
			{
			case CHANNEL_ITEM:
				dst.set( this.rot );
				break;
				
			default: this.animation.getRot( channel, dst );
			}
		}
		
		@Override
		public float getFactor( String channel ) { return this.animation.getFactor( channel ); }
		
		@Override
		public void tickInHand( E equipped, EnumHand hand ) { }
		
		@Override
		public void updateAnimationForRender( E renderDelegate, EnumHand hand ) { this.update(); }
		
		@Override
		public void prepareRenderInHandSP( E equipped, EnumHand hand ) { }
		
		@Override
		public boolean renderInHandSP( E equipped, EnumHand hand )
		{
			GL11.glPushMatrix();
			
			// Do customized rendering.
			final Minecraft mc = FMUMClient.MC;
			final EntityPlayer player = mc.player;
			
			// Copied from {@link EntityRenderer#renderHand(float, int)}.
			final EntityRenderer renderer = mc.entityRenderer;
			renderer.enableLightmap();
			
			/// Copied from {@link ItemRenderer#renderItemInFirstPerson(float)}. ///
			// {@link ItemRenderer#rotateArroundXAndY(float, float)}.
			final Mat4f mat = Mat4f.locate();
			PlayerPatchClient.instance.camera.getViewTransform( mat );
			glMulMatrix( mat );
			mat.release();
			
			GL11.glRotatef( 180F, 0F, 1F, 0F );
			RenderHelper.enableStandardItemLighting();
			
			// {@link ItemRenderer#setLightmap()}.
			final double eyeY = player.posY + player.getEyeHeight();
			final BlockPos blockPos = new BlockPos( player.posX, eyeY, player.posZ );
			int light = mc.world.getCombinedLight( blockPos, 0 );
			
			final float x = light & 0xFFFF;
			final float y = light >> 16;
			OpenGlHelper.setLightmapTextureCoords( OpenGlHelper.lightmapTexUnit, x, y );
			
			// {@link ItemRenderer#rotateArm(float)} is not applied to avoid shift.
			
			// TODO: Re-scale may not be needed. Do not forget that there is a disable pair call.
			GlStateManager.enableRescaleNormal();
			
			// Setup and render!
			GL11.glRotatef( 180F - player.rotationYaw, 0F, 1F, 0F );
			GL11.glRotatef( player.rotationPitch, 1F, 0F, 0F );
			this.doRenderInHandSP( equipped, hand );
			
			GlStateManager.disableRescaleNormal();
			RenderHelper.disableStandardItemLighting();
			/// End of {@link ItemRenderer#renderItemInFirstPerson(float)}. ///
			
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
			equipped.animator().getChannel( CHANNEL_ITEM, mat ); // TODO: equipped#animtor() should actually be #this
			glMulMatrix( mat );
			mat.release();
			
			FMUMClient.bindTexture( equipped.item().texture() );
			ItemModel.this.render();
		}
		
		/**
		 * Called in {@link #prepareRenderInHandSP(IEquippedItem, EnumHand)} to set up position and
		 * rotation before applying the animation.
		 */
		protected void updatePosRot()
		{
			this.pos.set( ItemModel.this.holdPos );
			this.rot.set( ItemModel.this.holdRot );
		}
	}
}
