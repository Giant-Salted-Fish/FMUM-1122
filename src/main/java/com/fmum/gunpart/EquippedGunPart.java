package com.fmum.gunpart;

import com.fmum.FMUM;
import com.fmum.input.IInput;
import com.fmum.input.Inputs;
import com.fmum.item.IItem;
import com.fmum.item.IMainEquipped;
import com.fmum.player.PlayerPatchClient;
import com.mojang.realmsclient.util.Pair;
import gsf.util.animation.IAnimator;
import gsf.util.render.GLUtil;
import gsf.util.render.IPose;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.Comparator;

public class EquippedGunPart implements IMainEquipped
{
	@SideOnly( Side.CLIENT )
	protected ArrayList< Runnable > in_hand_queue;
	
	protected EquippedGunPart() {
		FMUM.SIDE.runIfClient( () -> this.in_hand_queue = new ArrayList<>() );
	}
	
	@Override
	@SideOnly( Side.CLIENT )
	public IMainEquipped onInputUpdate( String name, IInput input, IItem item )
	{
		if ( input.getAsBool() && name.equals( Inputs.OPEN_MODIFY_VIEW ) ) {
			return new CEquippedModify( this, item );
		}
		
		return this;
	}
	
	@Override
	@SideOnly( Side.CLIENT )
	public void prepareRenderInHand( IItem item )
	{
		final IGunPart self = IGunPart.from( item );
		final IAnimator animator = this.EquippedGunPart$getInHandAnimator( item );
		this.EquippedGunPart$doPrepareRenderInHand( self, animator );
	}
	
	/**
	 * Stateless version of the {@link IMainEquipped#prepareRenderInHand(IItem)}.
	 * This allows other equipped wrappers to fully proxy the rendering.
	 */
	@SideOnly( Side.CLIENT )
	public void EquippedGunPart$doPrepareRenderInHand( IGunPart self, IAnimator animator )
	{
		// Clear previous in hand queue.
		this.in_hand_queue.clear();
		
		// Collect render callback.
		final ArrayList< IPreparedRenderer > renderers = new ArrayList<>();
		final IPose pose = animator.getChannel( CHANNEL_ITEM );
		self.IGunPart$prepareRender( pose, animator, renderers::add, ( p, l ) -> { }, ( p, r ) -> { } );
		renderers.stream()
			.map( pr -> pr.with( IPose.EMPTY ) )
			.sorted( Comparator.comparing( Pair::first ) )  // TODO: Reverse or not?
			.map( Pair::second )
			.forEachOrdered( this.in_hand_queue::add );
	}
	
	@SideOnly( Side.CLIENT )
	public IAnimator EquippedGunPart$getInHandAnimator( IItem item )
	{
		final GunPartType type = ( GunPartType ) item.getType();
		final IPose in_hand_setup = IPose.of( type.fp_pos, type.fp_rot );
		return ch -> ch.equals( CHANNEL_ITEM ) ? in_hand_setup : IPose.EMPTY;
	}
	
	@Override
	@SideOnly( Side.CLIENT )
	public boolean renderInHand( IItem item )
	{
		GL11.glPushMatrix();
		
		// Apply customized rendering.
		final Minecraft mc = Minecraft.getMinecraft();
		final EntityPlayerSP player = mc.player;
		
		// Copied from {@link EntityRenderer#renderHand(float, int)}.
		final EntityRenderer renderer = mc.entityRenderer;
		renderer.enableLightmap();
		
		// Copied from {@link ItemRenderer#renderItemInFirstPerson(float)}.
		// {@link ItemRenderer#rotateArroundXAndY(float, float)}.
		PlayerPatchClient.get().camera.getCameraSetup().glApply();
		
		GLUtil.glRotateYf( 180.0F );
		RenderHelper.enableStandardItemLighting();
		
		// {@link ItemRenderer#setLightmap()}.
		final double eye_pos_y = player.posY + player.getEyeHeight();
		final BlockPos block_pos = new BlockPos( player.posX, eye_pos_y, player.posZ );
		final int light = mc.world.getCombinedLight( block_pos, 0 );
		
		final float x = light & 0xFFFF;
		final float y = light >> 16;
		OpenGlHelper.setLightmapTextureCoords( OpenGlHelper.lightmapTexUnit, x, y );
		
		// {@link ItemRenderer#rotateArm(float)} is left out to avoid shift.
		
		// TODO: Re-scale may not be needed. Do not forget that there is a disable pair call.
		GlStateManager.enableRescaleNormal();
		
		// Setup and render!
		GLUtil.glRotateYf( 180.0F - player.rotationYaw );
		GLUtil.glRotateXf( player.rotationPitch );
		this._doRenderInHand( item );
		
		GlStateManager.disableRescaleNormal();
		RenderHelper.disableStandardItemLighting();
		// End of {@link ItemRenderer#renderItemInFirstPerson(float)}.
		
		renderer.disableLightmap();
		
		GL11.glPopMatrix();
		return true;
	}
	
	@SideOnly( Side.CLIENT )
	protected void _doRenderInHand( IItem item )
	{
//		Dev.cur().applyTransRot();
		this.in_hand_queue.forEach( Runnable::run );
	}
	
	@Override
	@SideOnly( Side.CLIENT )
	public boolean renderSpecificInHand( IItem item ) {
		return true;
	}
}
