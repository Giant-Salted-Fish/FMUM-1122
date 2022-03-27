package com.fmum.client.model.gun;

import java.util.ArrayList;

import org.lwjgl.opengl.GL11;

import com.fmum.client.EventHandlerClient;
import com.fmum.client.model.Animator;
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
	protected static final CoordSystem
		gunSys = new CoordSystem(),
		primaySys = new CoordSystem();
	
	/**
	 * Render info queue
	 */
	protected static final ArrayList<ModuleRenderInfo> infoQueue = new ArrayList<>();
	
	protected static final ArrayList<AimableRenderInfo> aimableQueue = new ArrayList<>();
	
	protected static final ArrayList<ScopeRenderInfo> scopeQueue = new ArrayList<>();
	
	public Animator animator = GunAnimator.INSTANCE;
	
	public final Vec3
		holdPos = new Vec3(87.5D / 160D, -72D / 160D, 14D / 160D),
		holdRot = new Vec3(-5D, 0D, 0D);
	
	public ModelGun() { }
	
	public ModelGun(Mesh mesh) { super(mesh); }
	
	public ModelGun(Mesh[] meshes) { super(meshes); }
	
	@Override
	public void renderTick(ItemStack stack, TypeInfo type)
	{
		super.renderTick(stack, type);
		
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
		
		// Get gun render coordinate system
		float smoother = Model.smoother;
		final Animator animator = this.animator;
		animator.setupRenderCoordSys(gunSys, smoother);
		
		// TODO: Apply view translation if is in modification mode
//		if(FMUMClient.operating == OpGunModification.INSTANCE)
//		{
//			
//		}
//		else
		{
			// Apply Movements controlled by animator
			animator.apply(gunSys, smoother);
			
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
		
		// TODO: Render scope mask if has
		
		
		// Restore world coordinate system
		GL11.glRotatef(-EventHandlerClient.actualCameraRoll, 1F, 0F, 0F); // animator.roll
		GL11.glRotatef(renderEyePitch, 0F, 0F, 1F);
		GL11.glRotatef(renderEyeYaw + 180F, 0F, 1F, 0F);
		GL11.glTranslated(-renderEyePosX, -renderEyePosY, -renderEyePosZ);
		
		// TODO: render modifying
//		if(FMUMClient.operating == )
//		{
//			
//		}
//		else
		{
			// TODO: Render arm
			
			
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
