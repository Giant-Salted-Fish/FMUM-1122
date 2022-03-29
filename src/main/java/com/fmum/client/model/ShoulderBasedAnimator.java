package com.fmum.client.model;

import org.lwjgl.opengl.GL11;

import com.fmum.client.FMUMClient;
import com.fmum.client.KeyManager;
import com.fmum.client.KeyManager.Key;
import com.fmum.common.type.TypeInfo;
import com.fmum.common.util.CoordSystem;
import com.fmum.common.util.Vec3;

import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MouseHelper;
import net.minecraft.util.math.MathHelper;

public class ShoulderBasedAnimator extends CamControlAnimator
{
	public final Vec3
		renderEyePos = new Vec3(),
		renderEyeRot = new Vec3();
	
	public final Vec3 eyeRotShift = new Vec3();
	
	/**
	 * Position of the shoulder relative to world coordinate system. Arm and item will be rendered
	 * in this coordinate system.
	 */
	public final Vec3 shoulderPos = new Vec3();
	
	/**
	 * Rotation of the shoulder relative to world coordinate system. In default is set as the same
	 * as player's rotation.
	 */
	public final Vec3 shoulderRot = new Vec3();
	
	@Override
	public void itemRenderTick(ItemStack stack, TypeInfo type, MouseHelper mouse)
	{
		super.itemRenderTick(stack, type, mouse);
		
		float smoother = getSmoother();
		final EntityPlayerSP player = getPlayer();
		Vec3 eyePos = this.renderEyePos;
		Vec3 eyeRot = this.renderEyeRot;
		
		// Update player's actual eye position
		double prev = player.prevPosX;
		eyePos.x = prev + (player.posX - prev) * smoother;
		prev = player.prevPosY;
		eyePos.y = prev + (player.posY - prev) * smoother + player.getEyeHeight();
		prev = player.prevPosZ;
		eyePos.z = prev + (player.posZ - prev) * smoother;
		
		// Get view rotation
		smoother = FMUMClient.settings.mouseSensitivity * 0.6F + 0.2F;
		smoother *= smoother * smoother * 8F * 0.15F;
		
		float changeYaw = mouse.deltaX * smoother;
		float eyeYaw = player.rotationYaw + 
		smoother *= mouse.deltaY;
		float eyePitch = MathHelper.clamp(
			player.rotationPitch + (getInvertMouse() ? smoother : -smoother),
			-90F,
			90F
		);
		
		// Clear mouse input to avoid changing walking direction if is looking around
		if((Key.CO.pressTime > 0 ? Key.CO_LOOK_AROUND : Key.LOOK_AROUND).pressTime > 0)
		{
			
		}
		
		eyeRot.x = getRenderCamRoll();
		eyeRot.y = eyeYaw;
		eyeRot.z = eyePitch;
		
		// Call shoulder coordinate system update
		this.updateShoulderPosRot(stack, type);
	}
	
	public void restoreGLWorldCoordSystem(float smoother)
	{
		// Restore world coordinate system
		Vec3 v = this.renderEyeRot;
		GL11.glRotated(-v.x, 1D, 0D, 0D);
		GL11.glRotated(v.z, 0D, 0D, 1D);
		GL11.glRotated(v.y + 90D, 0D, 1D, 0D);
		
		v = this.renderEyePos;
		GL11.glTranslated(-v.x, -v.y, -v.z);
	}
	
	/**
	 * Setup the coordinate system that this item should located in when rendering. In default it
	 * just setup the system with {@link #shoulderPos} and {@link #shoulderRot}.
	 * 
	 * @hackCode
	 *     In most cases this will be called to setup the coordinate system. hence in this
	 *     implementation it calls {@link CoordSystem#globalTrans(Vec3)} and
	 *     {@link CoordSystem#globalRot(double, double, double)} to do the transformation. If this
	 *     condition is not always true in your implementation, override and fall back to standard
	 *     transform {@link CoordSystem} functions.
	 * @param sys
	 *     Coordinate system to load render coordinate system into. Note that the input system could
	 *     be a raw system so it is recommended to call {@link CoordSystem#setDefault()} before
	 *     apply any transform.
	 */
	public void applyShoulderTransform(CoordSystem sys, float smoother)
	{
		sys.globalTrans(this.shoulderPos);
		
		Vec3 v = this.shoulderRot;
		sys.globalRot(v.x, -v.y - 90D, -v.z);
	}
	
	public void applyGLShoulderTransform(float smoother)
	{
		Vec3 v = this.shoulderPos;
		GL11.glTranslated(v.x, v.y, v.z);
		
		v = this.shoulderRot;
		GL11.glRotated(-v.y - 90D, 0D, 1D, 0D);
		GL11.glRotated(-v.z, 0D, 0D, 1D);
		GL11.glRotated(v.x, 1D, 0D, 0D);
	}
	
	protected void updateShoulderPosRot(ItemStack stack, TypeInfo type)
	{
		this.shoulderPos.set(this.renderEyePos);
		this.shoulderRot.set(this.renderEyeRot);
	}
	
	protected static boolean getInvertMouse() { return FMUMClient.settings.invertMouse; }
}
