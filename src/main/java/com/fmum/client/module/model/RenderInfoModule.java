package com.fmum.client.module.model;

import org.lwjgl.opengl.GL11;

import com.fmum.client.FMUMClient;
import com.fmum.client.ResourceManager;
import com.fmum.client.module.OpModification;
import com.fmum.common.module.InfoModule;
import com.fmum.common.module.ItemModular;
import com.fmum.common.module.Slot;
import com.fmum.common.module.TypeModular;
import com.fmum.common.util.CoordSystem;
import com.fmum.common.util.ObjPool;
import com.fmum.common.util.Vec3;

public class RenderInfoModule extends InfoModule
{
	public static final ObjPool<RenderInfoModule>
		pool = new ObjPool<>(() -> new RenderInfoModule());
	
	protected static final Vec3 vec = new Vec3();
	 
	public void render()
	{
		// Apply transform
		this.setupTransform();
		
		double scale = this.type.modelScale;
		GL11.glScaled(scale, scale, scale);
		
		// Bind texture and render
		FMUMClient.mc.renderEngine.bindTexture(type.getTexture(this.tag));
		this.type.model.render();
	}
	
	public void renderModifying(OpModification modify, int thisIndex)
	{
		// Not relevant to this attachment, fall back to normal render procedure
		if(thisIndex != modify.streamIndex)
		{
			this.render();
			return;
		}

		// Right index, render it in green if is selected
		final boolean selected = modify.selected();
		final boolean nonPrimaySelected = modify.nonPrimarySelected();
		final boolean paintMode = modify.isPaintMode();
		
		this.setupTransform();
		
		// Render indicator or preview module if not selected
		if(!selected)
		{
			GL11.glPushMatrix();
			{
				final Slot slot = this.type.slots[modify.loc[modify.locLen - 2]];
				GL11.glTranslated(slot.x, slot.y, slot.z);
				GL11.glRotated(slot.rotX, 1D, 0D, 0D);
				
				FMUMClient.mc.renderEngine.bindTexture(ResourceManager.TEXTURE_GREEN);
				
				if(modify.previewInvSlot != -1)
				{
					TypeModular typ = ((ItemModular)modify.previewStack.getItem()).getType();
					GL11.glTranslated(typ.getPos(modify.step, modify.offset, slot.stepLen), 0D, 0D);
					double scale = typ.modelScale;
					GL11.glScaled(scale, scale, scale);
					((ModelModular)typ.model).renderSelected();
				}
				else
				{
					GL11.glTranslated(slot.stepLen * slot.maxStep * 0.5D, 0D, 0D);
					((ModelModular)this.type.model).renderIndicator();
				}
			}
			GL11.glPopMatrix();
		}
		
		FMUMClient.mc.renderEngine.bindTexture(
			selected
			? (
				paintMode
				? this.type.getTexture(modify.dam)
				: nonPrimaySelected ? ResourceManager.TEXTURE_GREEN : this.type.getTexture(this.tag)
			)
			: this.type.getTexture(this.tag)
		);
		
		double scale = this.type.modelScale;
		GL11.glScaled(scale, scale, scale);
		if(nonPrimaySelected || paintMode)
			((ModelModular)this.type.model).renderSelected();
		else this.type.model.render();
	}
	
	/**
	 * Called when this info instance is released
	 */
	public void release() { pool.back(this); }
	
	protected final void setupTransform()
	{
		double[] v = this.sys.vec;
		GL11.glTranslated(
			v[CoordSystem.OFFSET + CoordSystem.X],
			v[CoordSystem.OFFSET + CoordSystem.Y],
			v[CoordSystem.OFFSET + CoordSystem.Z]
		);
		
		this.sys.getAngle(vec);
		GL11.glRotated(vec.y, 0D, 1D, 0D);
		GL11.glRotated(vec.z, 0D, 0D, 1D);
		GL11.glRotated(vec.x, 1D, 0D, 0D);
	}
}
