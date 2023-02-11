package com.mcwb.client.gun;

import java.util.ArrayList;

import com.mcwb.client.item.ModifiableItemAnimatorState;
import com.mcwb.client.item.ModifiableItemRenderer;
import com.mcwb.client.modify.IModifiableRenderer;
import com.mcwb.client.modify.ISecondaryRenderer;
import com.mcwb.client.player.ModifyOperationClient;
import com.mcwb.client.render.IAnimator;
import com.mcwb.client.render.IRenderer;
import com.mcwb.common.MCWB;
import com.mcwb.common.gun.IGunPart;
import com.mcwb.common.item.ModifiableItemType;
import com.mcwb.common.load.BuildableLoader;

import net.minecraft.util.EnumHand;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly( Side.CLIENT )
public class GunPartRenderer< T extends IGunPart > extends ModifiableItemRenderer< T >
	implements IModifiableRenderer< T >
{
	public static final BuildableLoader< IRenderer >
		LOADER = new BuildableLoader< IRenderer >(
			"gun_part", json -> MCWB.GSON.fromJson( json, GunPartRenderer.class )
		); // TODO: kind of weird as passing class works with ide but fails the compile
	
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
	
	@Override
	public void prepareRenderInHand( T contexted, EnumHand hand )
	{
		// Prepare render queue
		IN_HAND_RENDER_QUEUE.clear();
		IN_HAND_SECONDARY_RENDER_QUEUE.clear();
		contexted.base().prepareHandRenderer( // From wrapper
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
		contexted.base().prepareRenderer( // From wrapper
			RENDER_QUEUE,
			SECONDARY_RENDER_QUEUE,
			IAnimator.INSTANCE // TODO: proper animator
		);
		RENDER_QUEUE.forEach( IRenderer::render );
		SECONDARY_RENDER_QUEUE.forEach( IRenderer::render );
	}
	
	@Override
	public void renderModule( T contexted, IAnimator animator ) {
		contexted.modifyState().doRecommendedRender( contexted.texture(), this::render );
	}
	
	@Override
	protected void doRenderInHand( T contexted, EnumHand hand )
	{
		IN_HAND_RENDER_QUEUE.forEach( IRenderer::render );
		IN_HAND_SECONDARY_RENDER_QUEUE.forEach( IRenderer::render );
	}
	
	@Override
	protected ModifyOperationClient modifyOp() { return ModifiableItemType.MODIFY_OP; }
	
	@Override
	protected ModifiableItemAnimatorState animator( EnumHand hand ) {
		return GunAnimatorState.INSTANCE;
	}
}
