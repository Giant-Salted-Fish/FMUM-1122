package com.fmum.client.gun.model;

import java.util.Collection;
import java.util.TreeSet;
import java.util.function.Consumer;

import org.lwjgl.opengl.GL11;

import com.fmum.client.FMUMClient;
import com.fmum.client.model.Animator;
import com.fmum.client.model.AnimatorCamControl;
import com.fmum.client.model.ModelAlexArm;
import com.fmum.client.module.model.RenderInfoModule;
import com.fmum.common.module.InfoModule;
import com.fmum.common.module.TypeModular;
import com.fmum.common.type.TypeInfo;
import com.fmum.common.util.ArmTendency;
import com.fmum.common.util.CoordSystem;
import com.fmum.common.util.Mesh;
import com.fmum.common.util.Vec3;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.MouseHelper;

public class ModelGun extends ModelGrip
{
	/**
	 * Buffered systems for rendering
	 */
	protected static final CoordSystem gunSys = new CoordSystem();
	
	protected static final TreeSet<RenderInfoAimable> aimableQueue = new TreeSet<>();
	
	protected static final TreeSet<RenderInfoScope> scopeQueue = new TreeSet<>();
	
	public AnimatorGun animator = AnimatorGun.INSTANCE;
	
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
		dropCycle = AnimatorCamControl.dropCycle,
		dropAmpltCam = AnimatorCamControl.dropAmpltCam;
	
	/**
	 * @note
	 *     Gun animator will apply rotation on z axis of the camera on impact which and enhance the
	 *     impact effect and is not equipped in {@link AnimatorCamControl}. Hence the default value
	 *     is scaled to avoid over-effect.
	 */
	public double
		dropImpactAmpltCam = AnimatorCamControl.dropImpactAmpltCam * 0.75D;
	
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
		sprintAmpltGun_P = new Vec3(0D, 0.16D * 1.75D, 0.1D * 1.5D),
		sprintAmpltGun_R = new Vec3(-30D, -15D * 1.125D, 15D * 1.125D);
	
	public final Vec3
		walkAmpltCompensationGun_P = new Vec3(-0.15D, 0D, -0.15D),
		sprintAmpltCompensationGun_P = new Vec3(-0.3D, 0D, -0.3D);
	
	public final Vec3
		smoothViewRot_P = new Vec3(0D, 0.001D, -0.001D),
		smoothViewRot_R = new Vec3(0.15D, -0.15D, -0.2D);
	
	public final Vec3
		smoothMotion_P = new Vec3(-0.2D, -0.2D, -0.1D),
		smoothMotion_R = new Vec3(-25D, -15D, -15D);
	
	/**
	 * For right hand/arm
	 */
	public final Vec3 grabPos_R = new Vec3();
	public double grabHandRot_R = 0D;
	public double grabArmRot_R = 0D;
	
	public ModelGun() { }
	
	public ModelGun(Mesh... meshes) { super(meshes); }
	
	public ModelGun(Consumer<ModelGun> initializer) { initializer.accept(this); }
	
	@Override
	public RenderInfoModule prepareRenderInfo(NBTTagList tag, TypeModular type, CoordSystem sys)
	{
		RenderInfoAimable info = RenderInfoAimable.pool.poll();
		info.tag = tag;
		info.type = type;
		info.sys.set(sys);
		return info;
	}
	
	@Override
	public void updateRightHandTarPos(
		Animator ani,
		CoordSystem location,
		InfoModule info,
		ArmTendency dst
	) {
		vec.set(this.grabPos_R);
		info.sys.apply(vec, vec);
		location.apply(vec, vec);
		dst.setHandTarPos(vec);
		dst.setHandTarRotX(this.grabHandRot_R + ((AnimatorGun)ani).rot.getSmoothedX(1F));
		dst.setArmTarRotX(this.grabArmRot_R);
	}
	
	@Override
	public AnimatorGun getAnimatorFP() { return this.animator; }
	
	@Override
	public void itemRenderTick(ItemStack stack, TypeInfo type, MouseHelper mouse)
	{
		super.itemRenderTick(stack, type, mouse);
		
		// TODO: validate needary?
		final Collection<RenderInfoAimable> aimableQueue = this.getAimableQueue();
		final Collection<RenderInfoScope> scopeQueue = this.getScopeQueue();
		
		// Release resources located in last render tick
		aimableQueue.clear();
		scopeQueue.clear();
		
		// Find all sight and scope for later use
		for(RenderInfoModule info : this.getInfoQueue())
			if(info instanceof RenderInfoAimable)
			{
				aimableQueue.add((RenderInfoAimable)info);
				
				if(info instanceof RenderInfoScope)
					scopeQueue.add((RenderInfoScope)info);
			}
		
		// TODO: Get sight on use
//		AimableRenderInfo sightOnUse = aimableQueue.get(0);
		
		// TODO: Handle eyepiece texture rendering for scopes
		
	}
	
	@Override
	protected void doRenderFP(ItemStack stack, TypeInfo type)
	{
		super.doRenderFP(stack, type);
		
		// TODO
	}
	
	@Override
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
	
	protected Collection<RenderInfoAimable> getAimableQueue() { return aimableQueue; }
	
	protected Collection<RenderInfoScope> getScopeQueue() { return scopeQueue; }
	
	/** for test */
//		GL11.glPushMatrix();
//		{
//			GL11.glTranslatef(0F, 0F, 0F);
//			ModelDebugBox.INSTANCE.render();
//		}
//		GL11.glPopMatrix();
//		
//		GL11.glPushMatrix();
//		{
//			GL11.glTranslatef(1F, 0F, 0F);
//			ModelDebugBox.INSTANCE.render();
//		}
//		GL11.glPopMatrix();
//		
//		GL11.glPushMatrix();
//		{
//			GL11.glTranslatef(0F, 1F, 0F);
//			ModelDebugBox.INSTANCE.render();
//		}
//		GL11.glPopMatrix();
//		
//		GL11.glPushMatrix();
//		{
//			GL11.glTranslatef(0F, 0F, 1F);
//			ModelDebugBox.INSTANCE.render();
//		}
//		GL11.glPopMatrix();
	/** for test */
}
