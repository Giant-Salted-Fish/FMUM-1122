package com.mcwb.client.item;

import java.util.ArrayList;

import org.lwjgl.opengl.GL11;

import com.mcwb.client.gun.GunAnimatorState;
import com.mcwb.client.modify.IModifiableRenderer;
import com.mcwb.client.modify.ISecondaryRenderer;
import com.mcwb.client.player.OpModifyClient;
import com.mcwb.client.render.IAnimator;
import com.mcwb.client.render.IRenderer;
import com.mcwb.common.item.IItem;
import com.mcwb.common.item.ModifiableItemType;
import com.mcwb.common.modify.IModifiable;
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
	protected static final ArrayList< IRenderer > IN_HAND_RENDER_QUEUE = new ArrayList<>();
	protected static final ArrayList< ISecondaryRenderer >
		IN_HAND_SECONDARY_RENDER_QUEUE = new ArrayList<>();
	
	protected static final ArrayList< IRenderer > RENDER_QUEUE = new ArrayList<>();
	protected static final ArrayList< ISecondaryRenderer >
		SECONDARY_RENDER_QUEUE = new ArrayList<>();
	
	protected static final Vec3f MODIFY_POS = new Vec3f( 0F, 0F, 150F / 160F );
	
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
		// Prepare render queue
		IN_HAND_RENDER_QUEUE.clear();
		IN_HAND_SECONDARY_RENDER_QUEUE.clear();
		contexted.prepareHandRenderer(
			IN_HAND_RENDER_QUEUE,
			IN_HAND_SECONDARY_RENDER_QUEUE,
			this.animator( hand )
		);
		IN_HAND_SECONDARY_RENDER_QUEUE.sort( null );
		IN_HAND_SECONDARY_RENDER_QUEUE.forEach( ISecondaryRenderer::prepare );
	}
	
	@Override
	public void render( T contexted )
	{
		RENDER_QUEUE.clear();
		SECONDARY_RENDER_QUEUE.clear();
		contexted.prepareRenderer(
			RENDER_QUEUE,
			SECONDARY_RENDER_QUEUE,
			IAnimator.INSTANCE // TODO: proper animator
		);
		RENDER_QUEUE.forEach( IRenderer::render );
		SECONDARY_RENDER_QUEUE.forEach( IRenderer::render );
	}
	
	@Override
	public void renderModule( T contexted, IAnimator animator )
	{
		GL11.glPushMatrix(); {
		
		final float smoother = this.smoother();
		final ModifiableItemAnimatorState state = GunAnimatorState.INSTANCE; // TODO: replace this instance?
		animator.getChannel( ModifiableItemAnimatorState.CHANNEL_ITEM, smoother, state.m0 );
		animator.applyChannel( ModifiableItemAnimatorState.CHANNEL_INSTALL, smoother, state.m0 );
		glMultMatrix( state.m0 );
		
		contexted.modifyState().doRecommendedRender( contexted.texture(), this::render );
		
		} GL11.glPopMatrix();
	}
	
	@Override
	protected void doRenderInHand( T contexted, EnumHand hand )
	{
		IN_HAND_RENDER_QUEUE.forEach( IRenderer::render );
		IN_HAND_SECONDARY_RENDER_QUEUE.forEach( IRenderer::render );
	}
	
	protected OpModifyClient opModify() { return ModifiableItemType.OP_MODIFY; }
	
	@Override
	protected abstract ModifiableItemAnimatorState animator( EnumHand hand );
}
