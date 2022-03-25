package com.fmum.client.model.gun;

import org.lwjgl.opengl.GL11;

import com.fmum.client.ResourceManager;
import com.fmum.client.model.Animator;
import com.fmum.client.model.ModelModular;
import com.fmum.common.type.TypeInfo;
import com.fmum.common.util.Mesh;
import com.fmum.common.util.Vec3f;

import net.minecraft.item.ItemStack;

public class ModelGun extends ModelModular
{
	protected Animator animator = null; // TODO: default animator
	
	public final Vec3f
		holdPos = new Vec3f(87.5F / 160F, -72F / 160F, 14F / 160F),
		holdRot = new Vec3f(-5F, 0F, 0F);
	
	public ModelGun() { }
	
	public ModelGun(Mesh mesh) { super(mesh); }
	
	public ModelGun(Mesh[] meshes) { super(meshes); }
	
	@Override
	protected void doRenderFP(ItemStack stack, TypeInfo type)
	{
		Vec3f v = this.holdPos;
		GL11.glTranslatef(v.x, v.y, v.z);
		v = this.holdRot;
		GL11.glRotatef(v.y, 0F, 1F, 0F);
		GL11.glRotatef(v.z, 0F, 0F, 1F);
		GL11.glRotatef(v.x, 1F, 0F, 0F);
		
		float s = type.modelScale;
		GL11.glScalef(s, s, s);
		mc.renderEngine.bindTexture(ResourceManager.getTexture(type.texture));
		this.render();
	}
}
