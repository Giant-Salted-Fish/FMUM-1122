package com.fmum.client.module.model;

import org.lwjgl.opengl.GL11;

import com.fmum.client.module.OpModification;
import com.fmum.common.module.InfoModule;
import com.fmum.common.module.ItemModular;
import com.fmum.common.module.Slot;
import com.fmum.common.module.TagModular;
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
		// Do not forget to apply transform
		this.setupTransform();
		
		((ModelModular)this.type.model).renderModule(this.tag, this.type);
	}
	
	public void renderModifying(OpModification modify, int thisIndex)
	{
		// Not relevant to this attachment, fall back to normal render procedure
		if(thisIndex != modify.streamIndex)
		{
			this.render();
			return;
		}
		
		this.setupTransform();
		
		// Render indicator or preview module if not selected
		if(!modify.selected())
		{
			GL11.glPushMatrix();
			{
				final Slot slot = this.type.slots[0xFF & modify.loc[modify.locLen - 2]];
				GL11.glTranslated(slot.x, slot.y, slot.z);
				GL11.glRotated(slot.rotX, 1D, 0D, 0D);
				
				if(modify.previewInvSlot != -1)
				{
					final TypeModular typ = ((ItemModular)modify.previewStack.getItem()).getType();
					GL11.glTranslated(typ.getPos(modify.step, modify.offset, slot.stepLen), 0D, 0D);
					
					((ModelModular)typ.model).renderPreview(
						TagModular.getTag(modify.previewStack),
						typ,
						modify
					);
				}
				else
				{
					GL11.glTranslated(slot.stepLen * slot.maxStep * 0.5D, 0D, 0D);
					((ModelModular)this.type.model).renderIndicator();
				}
			}
			GL11.glPopMatrix();
		}
		
		// Render this module
		((ModelModular)this.type.model).renderModifyFocused(this.tag, this.type, modify);
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
