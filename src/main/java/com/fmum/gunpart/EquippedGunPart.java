package com.fmum.gunpart;

import com.fmum.FMUM;
import com.fmum.input.IInput;
import com.fmum.input.Inputs;
import com.fmum.item.IEquippedItem;
import com.fmum.item.IItem;
import com.fmum.module.IModule;
import com.fmum.module.ModifySession;
import com.fmum.player.PlayerPatchClient;
import com.fmum.render.IAnimator;
import com.fmum.render.IRenderCallback;
import gsf.util.animation.IPoseSetup;
import gsf.util.math.Vec3f;
import gsf.util.render.GLUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Optional;

public class EquippedGunPart implements IEquippedItem
{
	protected ModifySession operation;
	
	@SideOnly( Side.CLIENT )
	protected ArrayList< IRenderCallback > in_hand_queue;
	
	protected EquippedGunPart() {
	FMUM.SIDE.runIfClient( () -> this.in_hand_queue = new ArrayList<>() );
}
	
	@Override
	public IEquippedItem tickInHand( EnumHand hand, IItem held_item, EntityPlayer player )
	{
		return this;
	}
	
	@Override
	@SideOnly( Side.CLIENT )
	public IEquippedItem onInputUpdate( String name, IInput input, IItem item )
	{
		final boolean is_activation = input.getAsBool();
		if ( !is_activation ) {
			return this;
		}
		
		switch ( name )
		{
		case Inputs.OPEN_MODIFY_VIEW:
			return new EquippedModifying( this, item );
		}
		
		return this;
	}
	
	@Override
	@SideOnly( Side.CLIENT )
	public void prepareRenderInHand( EnumHand hand, IItem item )
	{
		final IGunPart self = this._getRenderDelegate( item );
		
		// Clear previous in hand queue.
		this.in_hand_queue.clear();
		
		// Collect render callback.
		final IAnimator animator = this._getInHandAnimator( hand, item );  // TODO: Replace with a real animator.
		self.IGunPart$prepareRender( -1, animator, this.in_hand_queue );
		
		// Sort render callback based on priority.
		// TODO: Reverse or not?
		this.in_hand_queue.sort( Comparator.comparing( rc -> rc.getPriority( IPoseSetup.EMPTY ) ) );
	}
	
	@SideOnly( Side.CLIENT )
	protected IGunPart _getRenderDelegate( IItem item )
	{
		final Optional< IModule > opt = item.lookupCapability( IModule.CAPABILITY );
		return ( IGunPart ) opt.orElseThrow( IllegalArgumentException::new );
	}
	
	@SideOnly( Side.CLIENT )
	protected IAnimator _getInHandAnimator( EnumHand hand, IItem item )
	{
		final GunPartType type = ( GunPartType ) item.getType();
		final Vec3f pos;
		if ( hand == EnumHand.MAIN_HAND ) {
			pos = type.fp_pos;
		}
		else
		{
			pos = new Vec3f();
			pos.set( type.fp_pos );
			pos.x = -pos.x;
		}
		final IPoseSetup in_hand_setup = IPoseSetup.of( pos, type.fp_rot, 0.0F );
		return ch -> ch.equals( CHANNEL_ITEM ) ? in_hand_setup : IPoseSetup.EMPTY;
	}
	
	@Override
	@SideOnly( Side.CLIENT )
	public boolean renderInHand( EnumHand hand, IItem item )
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
		this._doRenderInHand( hand, item );
		
		GlStateManager.disableRescaleNormal();
		RenderHelper.disableStandardItemLighting();
		// End of {@link ItemRenderer#renderItemInFirstPerson(float)}.
		
		renderer.disableLightmap();
		
		GL11.glPopMatrix();
		return true;
	}
	
	/**
	 * No need to push matrix here as caller should have done it.
	 */
	@SideOnly( Side.CLIENT )
	protected void _doRenderInHand( EnumHand hand, IItem item )
	{
//		Dev.cur().applyTransRot();
		this.in_hand_queue.forEach( IRenderCallback::render );
	}
	
	@Override
	@SideOnly( Side.CLIENT )
	public boolean renderSpecificInHand( EnumHand hand, IItem item ) {
		return true;
	}
}
