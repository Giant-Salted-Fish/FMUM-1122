package com.fmum.common.gun;

import com.fmum.client.FMUMClient;
import com.fmum.client.player.PlayerPatchClient;
import com.fmum.common.item.IEquippedItem;
import com.fmum.common.item.ItemType;
import com.fmum.common.load.IContentBuildContext;
import com.fmum.common.load.Model;
import com.fmum.common.module.IModuleSlot;
import com.fmum.common.module.IModuleType;
import com.fmum.common.module.Module;
import com.fmum.common.paintjob.IPaintableType;
import com.fmum.common.paintjob.IPaintjob;
import com.fmum.util.Category;
import com.fmum.util.GLUtil;
import com.fmum.util.Mat4f;
import com.google.gson.annotations.SerializedName;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class GunPartType extends ItemType implements IModuleType, IPaintableType
{
	private static final float[] DEFAULT_OFFSETS = { 0.0F };
	
	
	protected Category category;
	
	protected float param_scale = 1.0F;
	
	protected List< IModuleSlot > slots = Collections.emptyList();
	
	protected float[] offsets = DEFAULT_OFFSETS;
	
	protected List< IPaintjob > paintjobs = Collections.emptyList();
	
	@SideOnly( Side.CLIENT )
	@SerializedName( value = "model", alternate = "mesh" )
	protected Model model;
	
	@Override
	public void buildServerSide( IContentBuildContext ctx )
	{
		super.buildServerSide( ctx );
		
		IModuleType.REGISTRY.regis( this );
		IPaintableType.REGISTRY.regis( this );
		
		// Set it as not stackable.
		this.setMaxStackSize( 1 );
		
		// Check member variable setup.
		this.category = Optional.ofNullable( this.category )
			.orElseGet( () -> new Category( this.name ) );
		// TODO: Modify indicator.
		
		// Regis itself as the default paintjob.
		if ( this.paintjobs.isEmpty() ) {
			this.paintjobs = new ArrayList<>();
		}
		this.paintjobs.add( 0, () -> this.texture );
		
		// Apply param scale.
		this.slots.forEach( slot -> slot.scaleParam( this.param_scale ) );
		// TODO: Scale hit boxes.
	}
	
	@Override
	public void buildClientSide( IContentBuildContext ctx )
	{
		super.buildClientSide( ctx );
		
		ctx.regisMeshLoadCallback( this.model::loadMesh );
	}
	
	@Override
	public void injectPaintjob( IPaintjob paintjob ) {
		this.paintjobs.add( paintjob );
	}
	
	/**
	 * <p> {@inheritDoc} </p>
	 *
	 * <p> In default avoid to break the block when holding a this item in
	 * survive mode. </p>
	 */
	@Override
	public boolean onBlockStartBreak(
		@Nonnull ItemStack itemstack,
		@Nonnull BlockPos pos,
		@Nonnull EntityPlayer player
	) { return true; }
	
	/**
	 * <p> {@inheritDoc} </p>
	 *
	 * <p> In default avoid to break the block when holding this item in creative mode. </p>
	 */
	@Override
	public boolean canDestroyBlockInCreative(
		@Nonnull World world,
		@Nonnull BlockPos pos,
		@Nonnull ItemStack stack,
		@Nonnull EntityPlayer player
	) { return false; }
	
	
	protected class GunPart< I extends IGunPart< ? extends I > >
		extends Module< I > implements IGunPart< I >
	{
		protected short offset;
		protected short step;
		
		protected GunPart() { }
		
		protected GunPart( NBTTagCompound nbt )
		{
			super( nbt );
			
			// FIXME
		}
		
		@Override
		public int stackId() {
			throw new RuntimeException();
		}
		
		@Override
		public String name() {
			return GunPartType.this.name;
		}
		
		@Override
		public Category category() {
			return GunPartType.this.category;
		}
		
		@Override
		public int paintjobCount() {
			return GunPartType.this.paintjobs.size();
		}
		
		@Override
		public int slotCount() {
			return GunPartType.this.slots.size();
		}
		
		@Override
		public IModuleSlot getSlot( int idx ) {
			return GunPartType.this.slots.get( idx );
		}
		
		@Override
		public int offsetCount() {
			return GunPartType.this.offsets.length;
		}
		
		@Override
		public int offset() {
			return this.offset;
		}
		
		@Override
		public int step() {
			return this.step;
		}
		
		@Override
		public void deserializeNBT( NBTTagCompound nbt )
		{
			super.deserializeNBT( nbt );
			
			final int[] data = nbt.getIntArray( DATA_TAG );
			final int value = data[ super._dataSize() ];
			this.offset = ( short ) value;
			this.step = ( short ) ( value >>> 16 );
		}
		
		@Override
		public String toString() {
			return String.format( "Item<%s>", GunPartType.this );
		}
		
		@Override
		protected int _id() {
			return IModuleType.REGISTRY.getID( GunPartType.this );
		}
		
		@Override
		protected int _dataSize() {
			return 1 + super._dataSize();
		}
		
		@SideOnly( Side.CLIENT )
		protected ResourceLocation _texture() {
			return GunPartType.this.paintjobs.get( this.paintjob_idx ).texture();
		}
		
		
		protected class EquippedGunPart
			implements IEquippedItem< IGunPart< ? > >
		{
			@Override
			public IGunPart< ? > item() {
				return GunPart.this;
			}
			
			@Override
			@SideOnly( Side.CLIENT )
			public void prepareRenderInHand( EnumHand enumHand )
			{
			
			}
			
			@Override
			@SideOnly( Side.CLIENT )
			public boolean onRenderHand( EnumHand hand )
			{
				GL11.glPushMatrix(); {
				
				// Do customized rendering.
				final Minecraft mc = FMUMClient.MC;
				final EntityPlayer player = mc.player;
				
				// Copied from {@link EntityRenderer#renderHand(float, int)}.
				final EntityRenderer renderer = mc.entityRenderer;
				renderer.enableLightmap();
				
				/// Copied from {@link ItemRenderer#renderItemInFirstPerson(float)}. ///
				// {@link ItemRenderer#rotateArroundXAndY(float, float)}.
				final Mat4f mat = Mat4f.locate();
				PlayerPatchClient.instance.camera.getViewMat( mat );
				GLUtil.glMulMatrix( mat );
				mat.release();
				
				GLUtil.glRotateYf( 180.0F );
				RenderHelper.enableStandardItemLighting();
				
				// {@link ItemRenderer#setLightmap()}.
				final double eye_height = player.posY + player.getEyeHeight();
				final BlockPos blockPos = new BlockPos(
					player.posX, eye_height, player.posZ );
				int light = mc.world.getCombinedLight( blockPos, 0 );
				
				final float x = light & 0xFFFF;
				final float y = light >> 16;
				OpenGlHelper.setLightmapTextureCoords(
					OpenGlHelper.lightmapTexUnit, x, y );
				
				// {@link ItemRenderer#rotateArm(float)} is not applied to avoid shift.
				
				// TODO: Re-scale may not be needed. Do not forget that there is a disable pair call.
				GlStateManager.enableRescaleNormal();
				
				// Setup and render!
				GL11.glRotatef( 180F - player.rotationYaw, 0F, 1F, 0F );
				GL11.glRotatef( player.rotationPitch, 1F, 0F, 0F );
				this._doRenderInHand( hand );
				
				GlStateManager.disableRescaleNormal();
				RenderHelper.disableStandardItemLighting();
				/// End of {@link ItemRenderer#renderItemInFirstPerson(float)}. ///
				
				renderer.disableLightmap();
				
				} GL11.glPopMatrix();
				
				final boolean cancel_vanilla_hand_render = true;
				return cancel_vanilla_hand_render;
			}
			
			@Override
			@SideOnly( Side.CLIENT )
			public boolean onRenderSpecificHand( EnumHand hand ) {
				return true;
			}
			
			@SideOnly( Side.CLIENT )
			protected void _doRenderInHand( EnumHand hand )
			{
				final Mat4f mat = Mat4f.locate();
//				this.animator.getChannel( CHANNEL_ITEM, mat );
				GLUtil.glMulMatrix( mat );
				mat.release();
				
				GLUtil.bindTexture( GunPart.this._texture() );
				GunPartType.this.model.render();
			}
		}
	}
}
