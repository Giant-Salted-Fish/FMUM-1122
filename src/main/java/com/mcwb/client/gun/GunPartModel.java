package com.mcwb.client.gun;

import java.util.ArrayList;

import org.lwjgl.opengl.GL11;

import com.mcwb.client.item.ItemModel;
import com.mcwb.client.modify.IModifiableModel;
import com.mcwb.client.modify.IMultPassRenderer;
import com.mcwb.client.render.IModel;
import com.mcwb.common.gun.IContextedGunPart;
import com.mcwb.common.load.BuildableLoader;

import net.minecraft.util.EnumHand;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly( Side.CLIENT )
public class GunPartModel< T extends IContextedGunPart > extends ItemModel< T >
	implements IModifiableModel< T >
{
	public static final BuildableLoader< IModel >
		LOADER = new BuildableLoader<>( "gun_part", GunPartModel.class );
	
	/**
	 * Render queue is introduced here to help with rendering the objects with transparency
	 * TODO: maybe a better way to achieve multiple render pass
	 */
	protected static final ArrayList< IMultPassRenderer > RENDER_QUEUE = new ArrayList<>();
	protected static final ArrayList< IMultPassRenderer > BACK_QUEUE = new ArrayList<>();
	
	@Override
	public void onRenderTick( T contexted, EnumHand hand )
	{
		// Prepare render queue
		RENDER_QUEUE.clear();
		contexted.forEach( mod -> mod.prepareRenderer( RENDER_QUEUE, this.animator( hand ) ) );
	}
	
	@Override
	protected void doRenderInHand( T contexted, EnumHand hand )
	{
		// Repeat render pass until all modules has complete their work
		while( RENDER_QUEUE.size() > 0 )
		{
			BACK_QUEUE.clear();
			RENDER_QUEUE.forEach( renderer -> {
				// Push matrix and render each module // TODO: maybe move to #render method
				GL11.glPushMatrix(); {
				renderer.render( BACK_QUEUE );
				} GL11.glPopMatrix();
			} );
			
			RENDER_QUEUE.clear();
			RENDER_QUEUE.addAll( BACK_QUEUE );
		}
	}
}
