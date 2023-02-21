package com.mcwb.client.item;

import java.util.ArrayList;
import java.util.Collection;

import org.lwjgl.opengl.GL11;

import com.mcwb.client.modify.IDeferredPriorityRenderer;
import com.mcwb.client.modify.IDeferredRenderer;
import com.mcwb.client.modify.IModifiableRenderer;
import com.mcwb.client.player.OpModifyClient;
import com.mcwb.client.render.IAnimator;
import com.mcwb.common.item.IItem;
import com.mcwb.common.item.ModifiableItemType;
import com.mcwb.common.modify.IModifiable;
import com.mcwb.util.Mat4f;
import com.mcwb.util.Vec3f;

import net.minecraft.util.EnumHand;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly( Side.CLIENT )
public abstract class ModifiableItemRenderer< T extends IItem & IModifiable >
	extends ItemRenderer< T > implements IModifiableRenderer< T >
{
	/**
	 * Render queue is introduced here to help with rendering the objects with transparency
	 * FIXME: do not use same queue for first person hand and third-person view!
	 *     Because rendering scope glass texture could also trigger other third-person render and
	 *     this will break the queue state if they are the same
	 */
	protected static final ArrayList< IDeferredRenderer > IN_HAND_QUEUE_0 = new ArrayList<>();
	protected static final ArrayList< IDeferredPriorityRenderer >
		IN_HAND_QUEUE_1 = new ArrayList<>();
	
	protected static final ArrayList< IDeferredRenderer > RENDER_QUEUE_0 = new ArrayList<>();
	protected static final ArrayList< IDeferredPriorityRenderer >
		RENDER_QUEUE_1 = new ArrayList<>();
	
	private static final Vec3f MODIFY_POS = new Vec3f( 0F, 0F, 150F / 160F );
	
	protected Vec3f modifyPos = MODIFY_POS;
	
	@Override
	public void tickInHand( T contexted, EnumHand hand )
	{
		final ModifiableItemAnimatorState state = this.animator( hand );
		state.modifyOp = this.opModify();
		state.modifyPos = this.modifyPos;
		
		super.tickInHand( contexted, hand );
	}
	
	@Override
	public void prepareRenderInHand( T contexted, EnumHand hand )
	{
		// Clear previous state
		IN_HAND_QUEUE_0.forEach( IDeferredRenderer::release );
		IN_HAND_QUEUE_0.clear();
		IN_HAND_QUEUE_1.forEach( IDeferredPriorityRenderer::release );
		IN_HAND_QUEUE_1.clear();
		
		// Prepare render queue
		contexted.prepareHandRender(
			IN_HAND_QUEUE_0,
			IN_HAND_QUEUE_1,
			this.animator( hand )
		);
		IN_HAND_QUEUE_1.sort( null );
		IN_HAND_QUEUE_1.forEach( IDeferredPriorityRenderer::prepare );
	}
	
	@Override
	public void render( T contexted )
	{
		RENDER_QUEUE_0.forEach( IDeferredRenderer::release );
		RENDER_QUEUE_0.clear();
		RENDER_QUEUE_1.forEach( IDeferredPriorityRenderer::release );
		RENDER_QUEUE_1.clear();
		
		contexted.prepareRender(
			RENDER_QUEUE_0,
			RENDER_QUEUE_1,
			IAnimator.INSTANCE // TODO: proper animator
		);
		RENDER_QUEUE_0.forEach( IDeferredRenderer::render );
		RENDER_QUEUE_1.forEach( IDeferredPriorityRenderer::render );
	}
	
	@Override
	public void prepareRender(
		T contexted,
		IAnimator animator,
		Collection< IDeferredRenderer > renderQueue0,
		Collection< IDeferredPriorityRenderer > renderQueue1
	) {
		renderQueue0.add( () -> {
			GL11.glPushMatrix(); {
			
			final Mat4f mat = Mat4f.locate();
			final float smoother = this.smoother();
			animator.getChannel( CHANNEL_ITEM, smoother, mat );
			animator.applyChannel( CHANNEL_INSTALL, smoother, mat );
			glMultMatrix( mat );
			mat.release();
			
			contexted.modifyState().doRecommendedRender( contexted.texture(), this::render );
			
			} GL11.glPopMatrix();
		} );
	}
	
	@Override
	protected void doRenderInHand( T contexted, EnumHand hand )
	{
		IN_HAND_QUEUE_0.forEach( IDeferredRenderer::render );
		IN_HAND_QUEUE_1.forEach( IDeferredPriorityRenderer::render );
	}
	
	protected OpModifyClient opModify() { return ModifiableItemType.OP_MODIFY; }
	
	@Override
	protected abstract ModifiableItemAnimatorState animator( EnumHand hand );
}
