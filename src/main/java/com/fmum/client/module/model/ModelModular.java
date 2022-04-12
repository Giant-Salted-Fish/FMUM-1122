package com.fmum.client.module.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Consumer;

import org.lwjgl.opengl.GL11;

import com.fmum.client.ResourceManager;
import com.fmum.client.model.AnimatorCamControl;
import com.fmum.client.model.Model;
import com.fmum.client.model.ModelMeshBased;
import com.fmum.client.module.OpModification;
import com.fmum.common.module.Slot;
import com.fmum.common.module.TagModular;
import com.fmum.common.module.TypeModular;
import com.fmum.common.module.TypeModular.ModuleVisitor;
import com.fmum.common.type.TypeInfo;
import com.fmum.common.util.CoordSystem;
import com.fmum.common.util.Mesh;
import com.fmum.common.util.Vec3;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.MouseHelper;

public class ModelModular extends ModelMeshBased
{
	protected static final CoordSystem renderSys = CoordSystem.get();
	
	/**
	 * Render info queue
	 */
	protected static final ArrayList<RenderInfoModule> infoQueue = new ArrayList<>();
	
	protected static int streamIndex = 0;
	
	public final Vec3 modificationPos = Vec3.get(20D / 16D, -5D / 16D, 0D);
	
	public ModelModular() { }
	
	public ModelModular(Mesh... meshes) { super(meshes); }
	
	public ModelModular(Consumer<ModelModular> initializer) { initializer.accept(this); }
	
	public void renderModule(NBTTagList tag, TypeModular type)
	{
		bindTexture(type.getTexture(tag));
		final double scale = type.scale;
		GL11.glScaled(scale, scale, scale);
		this.render();
	}
	
	public void renderModifyFocused(NBTTagList tag, TypeModular type, OpModification modify)
	{
		final boolean paintMode = modify.isPaintMode();
		final boolean nonPrimarySelected = modify.nonPrimarySelected();
		
		bindTexture(
			modify.selected()
			? (
				paintMode
				? type.getTexture(modify.dam)
				: (
					nonPrimarySelected
					? modify.valid ? ResourceManager.TEXTURE_GREEN : ResourceManager.TEXTURE_RED
					: type.getTexture(tag)
				)
			)
			: type.getTexture(tag)
		);
		
		final double scale = type.scale;
		GL11.glScaled(scale, scale, scale);
		if(nonPrimarySelected || paintMode)
			this.renderHighLighted();
		else this.render();
	}
	
	public void renderPreview(NBTTagList tag, TypeModular type, OpModification modify)
	{
		// Render modules installed on this module
		for(int i = type.slots.length; i-- > 0; )
		{
			final NBTTagList slotTag = (NBTTagList)tag.get(1 + i);
			int j = slotTag.tagCount();
			if(j == 0) continue;
			
			final Slot slot = type.slots[i];
			GL11.glPushMatrix();
			{
				GL11.glTranslated(slot.x, slot.y, slot.z);
				GL11.glRotated(slot.rotX, 1D, 0D, 0D);
				while(j-- > 0)
				{
					final NBTTagList moduleTag = (NBTTagList)slotTag.get(j);
					final TypeModular moduleType = TagModular.getType(moduleTag);
					GL11.glPushMatrix();
					{
						GL11.glTranslated(
							moduleType.getPosX(TagModular.getStates(moduleTag), slot.stepLen),
							0D,
							0D
						);
						((ModelModular)moduleType.model).renderPreview(
							moduleTag,
							moduleType,
							modify
						);
					}
					GL11.glPopMatrix();
				}
			}
			GL11.glPopMatrix();
		}
		
		bindTexture(modify.valid ? ResourceManager.TEXTURE_GREEN : ResourceManager.TEXTURE_RED);
		final double scale = type.scale;
		GL11.glScaled(scale, scale, scale);
		this.renderHighLighted();
	}
	
	@Override
	public AnimatorCamControl getAnimatorFP() { return AnimatorCamControl.INSTANCE; }
	
	@Override
	public void itemRenderTick(ItemStack stack, TypeInfo type, MouseHelper mouse)
	{
		super.itemRenderTick(stack, type, mouse);
		
		final Collection<RenderInfoModule> infoQueue = this.getInfoQueue();
		
		// Release resource located in last render tick
		for(RenderInfoModule info : infoQueue)
			info.release();
		infoQueue.clear();
		
		// Make sure the tags are ready before rendering
		if(!TagModular.validateTag(stack)) return;
		
		// Position module into shoulder coordinate system
		final float smoother = Model.smoother;
		final AnimatorCamControl animator = this.getAnimatorFP();
		final CoordSystem renderSys = this.getRenderSystem();
		renderSys.setDefault();
		animator.setupRenderTransform(renderSys, smoother);
		
		// Apply view translation if is in modification mode
		if(getOperating() == OpModification.INSTANCE)
		{
			double progress = OpModification.INSTANCE.getSmoothedProgress(smoother);
			vec.set(this.modificationPos);
			vec.scale(progress);
			renderSys.trans(vec.x, vec.y, 0D);
			
			double yaw = animator.renderRot.y % 360D;
			if(yaw > 180D) yaw -= 360D;
			else if(yaw < -180D) yaw += 360D;
			renderSys.rot(yaw * progress, CoordSystem.Y);
			renderSys.rot(animator.renderRot.z, CoordSystem.Z);
			renderSys.submitRot();
			renderSys.trans(-vec.z, CoordSystem.NORM_Z);
			
			// Suppress original animation upon progress
			progress = 1D - progress;
			sys.setDefault();
			animator.applyTransform(sys, smoother);
			sys.get(vec, CoordSystem.OFFSET);
			vec.scale(progress);
			renderSys.trans(vec);
			sys.getAngle(vec);
			vec.scale(progress);
			renderSys.rot(vec);
			renderSys.submitRot();
			
			// Prepare stream index by render stream
			this.setStreamIndex(0);
			((TypeModular)type).stream(
				TagModular.getTag(stack),
				renderSys,
				this.getModifyingVisitor()
			);
		}
		else
		{
			// Apply animation controlled by animator
			animator.applyTransform(renderSys, smoother);
			
			// Buffer information of all modules installed on this module for future rendering
			((TypeModular)type).stream(TagModular.getTag(stack), renderSys, this.getVisitor());
		}
	}
	
	@Override
	protected void doRenderFP(ItemStack stack, TypeInfo type)
	{
		// Not yet ready for render, skip
		final Collection<RenderInfoModule> infoQueue = this.getInfoQueue();
		if(infoQueue.size() == 0) return;
		
		// TODO: validate if frequently used or not
		float smoother = Model.smoother;
		
		// Restore world coordinate system
		this.getAnimatorFP().restoreGLWorldCoordSystem(smoother);
		
		if(getOperating() == OpModification.INSTANCE)
		{
			int i = 0;
			for(RenderInfoModule info : infoQueue)
			{
				GL11.glPushMatrix(); {
					info.renderModifying(OpModification.INSTANCE, i++);
				} GL11.glPopMatrix();
			}
		}
		else
		{
			GL11.glPushMatrix(); {
				this.renderArms(smoother);
			} GL11.glPopMatrix();
			
			for(RenderInfoModule info : infoQueue)
			{
				GL11.glPushMatrix(); {
					info.render();
				} GL11.glPopMatrix();
			}
		}
	}
	
	// TODO: override in aimable and scope model
	public RenderInfoModule prepareRenderInfo(NBTTagList tag, TypeModular type, CoordSystem sys)
	{
		RenderInfoModule info = RenderInfoModule.get();
		info.tag = tag;
		info.type = type;
		info.sys.set(sys);
		return info;
	}
	
	protected void renderArms(float smoother)
	{
		// TODO?
	}
	
	protected Collection<RenderInfoModule> getInfoQueue() { return infoQueue; }
	
	protected CoordSystem getRenderSystem() { return renderSys; }
	
	protected int getStreamIndex() { return streamIndex; }
	
	protected void setStreamIndex(int index) { streamIndex = index; }
	
	protected ModuleVisitor getModifyingVisitor()
	{
		return (tag, typ, sys) -> {
			// Check stream index
			final int index = this.getStreamIndex();
			final OpModification modify = OpModification.INSTANCE;
			if(index == modify.streamIndex && modify.nonPrimarySelected())
			{
				final int[] states = TagModular.getStates(tag);
				final double stepLen = modify.baseType.slots[
					0xFF & modify.loc[modify.locLen - 2]
				].stepLen;
				sys.trans(
					typ.getPosX(modify.step, modify.offset, stepLen)
						- typ.getPosX(states, stepLen),
					CoordSystem.NORM_X
				);
			}
			
			// FIXME: maybe stream index should be right value?
			// Increase stream index
			this.setStreamIndex(index + 1);
			
			this.getInfoQueue().add(
				((ModelModular)typ.model).prepareRenderInfo(tag, typ, sys)
			);
			return false;
		};
	}
	
	protected ModuleVisitor getVisitor()
	{
		return (tag, typ, sys) -> {
			this.getInfoQueue().add(
				((ModelModular)typ.model).prepareRenderInfo(tag, typ, sys)
			);
			return false;
		};
	}
	
	protected void renderIndicator()
	{
		glowOn();
		ModelIndicator.INSTANCE.render();
		glowOff();
	}
}
