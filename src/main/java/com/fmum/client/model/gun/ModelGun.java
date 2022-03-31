package com.fmum.client.model.gun;

import java.util.ArrayList;

import org.lwjgl.opengl.GL11;

import com.fmum.client.FMUMClient;
import com.fmum.client.model.Animator;
import com.fmum.client.model.CamControlAnimator;
import com.fmum.client.model.Model;
import com.fmum.client.model.ModelAlexArm;
import com.fmum.client.model.ModelDebugBox;
import com.fmum.client.model.module.ModelModular;
import com.fmum.client.model.module.ModuleRenderInfo;
import com.fmum.client.model.oc.ModelFNMK20SSR;
import com.fmum.common.gun.TagGun;
import com.fmum.common.module.ModuleInfo;
import com.fmum.common.module.TagModular;
import com.fmum.common.module.TypeModular;
import com.fmum.common.type.TypeInfo;
import com.fmum.common.util.ArmTendency;
import com.fmum.common.util.CoordSystem;
import com.fmum.common.util.Mesh;
import com.fmum.common.util.ObjPool;
import com.fmum.common.util.Vec3;

import net.minecraft.item.ItemStack;
import net.minecraft.util.MouseHelper;

public class ModelGun extends ModelGrip
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
		holdPos = new Vec3(87.5D / 160D, -72D / 160D, 14D / 160D),
		holdRot = new Vec3(-5D, 0D, 0D),
		aimPos = new Vec3(81.5D / 160D, -64D / 160D, 0D),
		aimRot = new Vec3(),
		crouchPos = new Vec3(87.5D / 160D, -64D / 160D, 28D / 160D),
		crouchRot = new Vec3(-45D, 0D, 0D),
		sprintPos = new Vec3(81.5D / 160D, -96D / 160D, -5D / 160D),
		sprintRot = new Vec3(-25D, 27.5D, -20D);
	
	public final Vec3
		holdMoveOffset = new Vec3(-0.5D / 16D, -1.5D / 16D, 0D),
		aimMoveOffset = new Vec3(-0.5D / 16D, 0D, 0D),
		crouchMoveOffset = new Vec3(this.holdMoveOffset),
		sprintMoveOffset = new Vec3(this.holdMoveOffset);
	
	public final Vec3
		holdShoulderOffset = new Vec3(0.001D, 0.0005D, 0D),
		aimShoulderOffset = new Vec3(0.0005D, 0D, 0D),
		crouchShoulderOffset = new Vec3(this.holdShoulderOffset),
		sprintShoulderOffset = new Vec3(this.holdShoulderOffset);
	
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
	
	/**
	 * Player's acceleration on y axis could be very intensive when stepping onto stairs. Hence it
	 * is recommended to scale it down to avoid hard impacts on camera.
	 */
	public double camDropAccMult = 0.5D;
	
	public double
		walkCycle = Math.PI * 0.6D,
		crouchWalkCycle = Math.PI;
	
	public final Vec3 motionAmpltCam = new Vec3(-7.5D, 7.5D, -10D);
	
	public final Vec3
		walkAmpltCam = new Vec3(-3D, 1.5D, 3D),
		sprintAmpltCam = new Vec3(-3D, 1.5D, 3D);
	
	public final Vec3
		walkAmpltGun_P = new Vec3(0D, 0.05D, 0.05D),
		walkAmpltGun_R = new Vec3(-15D, 0D, 5D),
		sprintAmpltGun_P = new Vec3(0D, 0.16D, 0.1D),
		sprintAmpltGun_R = new Vec3(-30D, -15D, 15D);
	
	public final Vec3
		walkAmpltCompensationGun_P = new Vec3(-0.15D, 0D, -0.15D),
		sprintAmpltCompensationGun_P = new Vec3(-0.3D, 0D, -0.3D);
	
	public final Vec3
		smoothViewRot_P = new Vec3(0D, 0.001D, -0.001D),
		smoothViewRot_R = new Vec3(0.15D, -0.15D, -0.2D);
	
	public final Vec3
		smoothMotion_P = new Vec3(-0.2D, -0.2D, -0.2D),
		smoothMotion_R = new Vec3(-25D, -15D, -15D);
	
	/**
	 * For right hand/arm
	 */
	public final Vec3 grabPos_R = new Vec3();
	public double grabHandRot_R = 0D;
	public double grabArmRot_R = 0D;
	
	public ModelGun() { }
	
	public ModelGun(Mesh mesh) { super(mesh); }
	
	public ModelGun(Mesh[] meshes) { super(meshes); }
	
	@Override
	public void updateRightHandTarPos(
		Animator ani,
		CoordSystem location,
		ModuleInfo info,
		ArmTendency dest
	) {
		vec.set(this.grabPos_R);
		info.apply(vec, vec);
		location.apply(vec, vec);
		dest.setHandTarPos(vec);
		dest.setHandTarRotX(this.grabHandRot_R + ((GunAnimator)ani).rot.getSmoothedX(1F));
		dest.setArmTarRotX(this.grabArmRot_R);
	}
	
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
		animator.setupRenderTransform(gunSys, smoother);
		
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
				gunSys.trans(20D / 16D, 2D / 16D, 0D);
				double viewRotYaw = animator.renderRot.y % 360D;
				if(viewRotYaw > 180D) viewRotYaw -= 360D; // TODO: validate
				else if(viewRotYaw < -180D) viewRotYaw += 360D;
				gunSys.rot(viewRotYaw, CoordSystem.Y);
				gunSys.rot(animator.renderRot.z, CoordSystem.Z);
				gunSys.submitRot();
//				gunSys.trans(0D, 0D, 0D);
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
			{ this.renderArms(smoother); }
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
	
	protected void renderArms(float smoother)
	{
		this.animator.setupGLRenderTransform(smoother);
		
		/** for test */
		if(FMUMClient.manualMode)
		{
			GL11.glTranslated(20D / 16D, 2D / 16D, 0D);
			double viewRotYaw = animator.renderRot.y % 360D;
			if(viewRotYaw > 180D) viewRotYaw -= 360D; // TODO: validate
			else if(viewRotYaw < -180D) viewRotYaw += 360D;
			GL11.glRotated(animator.renderRot.z, 0D, 0D, 1D);
			GL11.glRotated(viewRotYaw, 0D, 1D, 0D);
//			GL11.glTranslated(0D, 0D, 0D);
		}
		/** for test */
		
		// Left arm
		GL11.glPushMatrix();
		{
			this.animator.leftArmPos.getSmoothedPos(vec, smoother);
			GL11.glTranslated(vec.x, vec.y, vec.z);
			
			this.animator.leftArmPos.getSmoothedRot(vec, smoother);
			GL11.glRotated(vec.y, 0D, 1D, 0D);
			GL11.glRotated(vec.z, 0D, 0D, 1D);
			GL11.glRotated(vec.x, 1D, 0D, 0D);
			
			ModelAlexArm.INSTANCE.render();
		}
		GL11.glPopMatrix();
		
		// Right arm
		this.animator.rightArmPos.getSmoothedPos(vec, smoother);
		GL11.glTranslated(vec.x, vec.y, vec.z);
		
		this.animator.rightArmPos.getSmoothedRot(vec, smoother);
		GL11.glRotated(vec.y, 0D, 1D, 0D);
		GL11.glRotated(vec.z, 0D, 0D, 1D);
		GL11.glRotated(vec.x, 1D, 0D, 0D);
		
		ModelAlexArm.INSTANCE.render();
	}
	
	@Override
	protected ModuleRenderInfo getRenderInfo() { return AimableRenderInfo.pool.poll(); }
}
