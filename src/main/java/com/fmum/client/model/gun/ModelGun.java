package com.fmum.client.model.gun;

import java.util.ArrayList;

import org.lwjgl.opengl.GL11;

import com.fmum.client.FMUMClient;
import com.fmum.client.model.CamControlAnimator;
import com.fmum.client.model.Model;
import com.fmum.client.model.ModelDebugBox;
import com.fmum.client.model.module.ModelModular;
import com.fmum.client.model.module.ModuleRenderInfo;
import com.fmum.common.gun.TagGun;
import com.fmum.common.module.TagModular;
import com.fmum.common.module.TypeModular;
import com.fmum.common.type.TypeInfo;
import com.fmum.common.util.CoordSystem;
import com.fmum.common.util.Mesh;
import com.fmum.common.util.ObjPool;
import com.fmum.common.util.Vec3;

import net.minecraft.item.ItemStack;
import net.minecraft.util.MouseHelper;

public class ModelGun extends ModelModular
{
	/**
	 * A pool that buffers scope eyepiece textures
	 */
	protected static final ObjPool<Integer> scopeTexturePool = new ObjPool<>(
		() -> 1 // FIXME: initialize a texture
	);
	
	/** TODO: validate needary
	 * Buffered systems for rendering
	 */
	protected static final CoordSystem gunSys = new CoordSystem();
	
	/**
	 * Render info queue
	 */
	protected static final ArrayList<ModuleRenderInfo> infoQueue = new ArrayList<>();
	
	protected static final ArrayList<AimableRenderInfo> aimableQueue = new ArrayList<>();
	
	protected static final ArrayList<ScopeRenderInfo> scopeQueue = new ArrayList<>();
	
	public GunAnimator animator = GunAnimator.INSTANCE;
	
//	public double
//		shoulderOffsetX = 0.5D / 16D,
//		shoulderOffsetY = -1D / 16D;
	
	public final Vec3
		holdPos = new Vec3(
			87.5D / 160D, // - this.shoulderOffsetX,
			-72D / 160D,  // - this.shoulderOffsetY,
			14D / 160D
		),
		holdRot = new Vec3(-5D, 0D, 0D);
	
	public final Vec3
		smoothViewRot_P = new Vec3(0D, 0.001D, -0.001D),
		smoothViewRot_R = new Vec3(0.15D, -0.15D, -0.2D);
	
	public double
		breathCycleBase = 0.75D / 16D,
		breathCycleIncr = 0.75D / 16D;
	
	public final Vec3
		breathAmpltCamBase = new Vec3(0D, 5D / 16D, 5D / 16D),
		breathAmpltCamIncr = new Vec3(0D, 0D, 0D);
	
	public final Vec3
		breathAmpltGunBase_P = new Vec3(0D, 0D, 0D),
		breathAmpltGunIncr_P = new Vec3(0D, 0D, 0D),
		breathAmpltGunBase_R = new Vec3(-5D / 16D, -5D / 16D, -5D / 16D),
		breathAmpltGunIncr_R = new Vec3(0D, 0D, 0D);
	
	public double
		dropCycle = CamControlAnimator.dropCycle,
		dropAmpltCam = CamControlAnimator.dropAmpltCam;
	
	/**
	 * @note
	 *     Gun animator will apply rotation on z axis of the camera on impact which and enhance the
	 *     impact effect and is not equipped in {@link CamControlAnimator}. Hence the default value
	 *     is scaled to avoid over-effect.
	 */
	public double
		dropImpactAmpltCam = CamControlAnimator.dropImpactAmpltCam * 0.75D;
	
	public double
		walkCycle = Math.PI * 0.6D,
		crouchWalkCycle = Math.PI;
	
	public double dropImpactAccCamMult = 0.5D;
	
	public final Vec3 motionAmpltCam = new Vec3(-7.5D, 7.5D, -10D);
	
	public final Vec3
		walkAmpltCam = new Vec3(-3D, 1.5D, 3D),
		sprintAmltCam = new Vec3(-3D, 1.5D, 3D);
	
	public ModelGun() { }
	
	public ModelGun(Mesh mesh) { super(mesh); }
	
	public ModelGun(Mesh[] meshes) { super(meshes); }
	
	@Override
	public GunAnimator getAnimatorFP() { return this.animator; }
	
	@Override
	public void itemRenderTick(ItemStack stack, TypeInfo type, MouseHelper mouse)
	{
		super.itemRenderTick(stack, type, mouse);
		
		// Return out dated occupied info instances
		for(int i = scopeQueue.size(); --i >= 0; )
			if(scopeQueue.get(i).scopeTexture != 0)
			{
				ScopeRenderInfo scope = scopeQueue.get(i);
				scopeTexturePool.back(scope.scopeTexture);
				scope.scopeTexture = 0;
			}
		scopeQueue.clear();
		aimableQueue.clear();
		for(int i = infoQueue.size(); --i >= 0; infoQueue.remove(i).release());
		
		// Make sure gun tags are ready before rendering
		if(!TagGun.validateTag(stack)) return;
		
		// Position gun into shoulder coordinate system
		float smoother = Model.smoother;
		final GunAnimator animator = this.animator;
		gunSys.setDefault();
		animator.applyRenderTransform(gunSys, smoother);
		
		// TODO: Apply view translation if is in modification mode
//		if(FMUMClient.operating == OpGunModification.INSTANCE)
//		{
//			
//		}
//		else
		{
			/** for test */
			if(FMUMClient.manualMode)
			{
				gunSys.trans(20F / 16F, 2F / 16F, 5F / 16F);
				double viewRotYaw = animator.renderRot.y % 360D;
				if(viewRotYaw > 180D) viewRotYaw -= 360D; // TODO: validate
				else if(viewRotYaw < -180D) viewRotYaw += 360D;
				gunSys.rot(viewRotYaw, CoordSystem.Y);
				gunSys.rot(animator.renderRot.z, CoordSystem.Z);
				gunSys.submitRot();
				gunSys.trans(0F, 0F, -5F / 16F);
			}
			/** for test */
			
			// Apply Movements controlled by animator
			animator.applyTransform(gunSys, smoother);
			
			// Get position of each attachment on gun, buffer them for future rendering
			((TypeModular)type).stream(
				TagModular.getTag(stack),
				gunSys,
				(tag, typ, sys) -> {
					// Reserve information for later rendering
					ModuleRenderInfo info
						= ((ModelModular)type.model).prepareRenderInfo(tag, typ, sys);
					infoQueue.add(info);
					
					if(info instanceof ScopeRenderInfo)
						scopeQueue.add((ScopeRenderInfo)info);
					else if(info instanceof AimableRenderInfo)
						aimableQueue.add((AimableRenderInfo)info);
				}
			);
		}
		
		// TODO: Get sight on use
//		AimableRenderInfo sightOnUse = aimableQueue.get(0);
		
		// TODO: Handle eyepiece texture rendering for scopes
		
	}
	
	@Override
	protected void doRenderFP(ItemStack stack, TypeInfo type)
	{
		// Not yet ready for render, skip
		if(infoQueue.size() == 0) return;
		
		// TODO: validate if frequently used or not
		float smoother = Model.smoother;
		GunAnimator animator = this.animator;
		
		// TODO: Render scope mask if has
		
		
		// Restore world coordinate system
		animator.restoreGLWorldCoordSystem(smoother);
		
		// TODO: render modifying
//		if(FMUMClient.operating == )
//		{
//			
//		}
//		else
		{
			// TODO: Render arm
			
			// Setup shoulder coordinate system to render the arms
			GL11.glPushMatrix();
			{
				animator.applyGLRenderTransform(smoother);
				
				
			}
			GL11.glPopMatrix();
			
			for(int i = 0, size = infoQueue.size(); i < size; ++i)
			{
				GL11.glPushMatrix(); {
					infoQueue.get(i).render();
				} GL11.glPopMatrix();
			}
		}
		
		GL11.glPushMatrix();
		{
			GL11.glTranslatef(0F, 0F, 0F);
			ModelDebugBox.INSTANCE.render();
		}
		GL11.glPopMatrix();
		
		GL11.glPushMatrix();
		{
			GL11.glTranslatef(1F, 0F, 0F);
			ModelDebugBox.INSTANCE.render();
		}
		GL11.glPopMatrix();
		
		GL11.glPushMatrix();
		{
			GL11.glTranslatef(0F, 1F, 0F);
			ModelDebugBox.INSTANCE.render();
		}
		GL11.glPopMatrix();
		
		GL11.glPushMatrix();
		{
			GL11.glTranslatef(0F, 0F, 1F);
			ModelDebugBox.INSTANCE.render();
		}
		GL11.glPopMatrix();
		
//		Vec3 v = this.holdPos;
//		GL11.glTranslated(v.x, v.y, v.z);
//		v = this.holdRot;
//		GL11.glRotated(v.y, 0D, 1D, 0D);
//		GL11.glRotated(v.z, 0D, 0D, 1D);
//		GL11.glRotated(v.x, 1D, 0D, 0D);
//		
//		double s = type.modelScale;
//		GL11.glScaled(s, s, s);
//		mc.renderEngine.bindTexture(ResourceManager.getTexture(type.texture));
//		this.render();
	}
	
	@Override
	protected ModuleRenderInfo getRenderInfo() { return AimableRenderInfo.pool.poll(); }
}
