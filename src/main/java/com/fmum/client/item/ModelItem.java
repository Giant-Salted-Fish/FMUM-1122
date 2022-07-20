package com.fmum.client.item;

import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.Project;

import com.fmum.client.FMUMClient;
import com.fmum.client.render.Model;
import com.fmum.common.item.MetaItem;
import com.fmum.common.util.Mesh;

import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;

public class ModelItem extends Model implements RenderableItem
{
	public ModelItem() { }
	
	public ModelItem( Mesh... meshes ) { super( meshes ); }
	
	/**
	 * This method helps to setup the basic projection and swap x and z axis before you do actual
	 * rendering. Note that it is not recommended for you to directly override this method to do
	 * your own rendering. Use {@link #doRenderInHand(ItemStack, MetaItem)} instead.
	 */
	@Override
	public void renderInHand( ItemStack stack, MetaItem meta )
	{
		// Re-setup projection matrix
		GL11.glMatrixMode( GL11.GL_PROJECTION );
		GL11.glLoadIdentity();
		Project.gluPerspective(
			this.fovModifier( this.smoother() ),
			( float ) FMUMClient.mc.displayWidth / FMUMClient.mc.displayHeight,
			0.05F, // TODO: maybe smaller this value to avoid seeing through the parts
			FMUMClient.settings.renderDistanceChunks * 16 * MathHelper.SQRT_2
		);
		GL11.glMatrixMode( GL11.GL_MODELVIEW );
		
		// Swap y and z axis
		GL11.glRotated( 90D, 0D, 1D, 0D );
		
		/** for test 
		double[] d = FMUMClient.testList.get( 0 ).testValue;
		GL11.glTranslated( d[ 0 ], d[ 1 ], d[ 2 ] );
		GL11.glRotated( d[ 4 ], 0D, 1D, 0D );
		GL11.glRotated( d[ 5 ], 0D, 0D, 1D );
		GL11.glRotated( d[ 3 ], 1D, 0D, 0D );
		/** for test */
		
		this.doRenderInHand( stack, meta );
	}
	
	/**
	 * Called by {@link #renderInHand(ItemStack, MetaItem)} to do actual rendering after it helps to
	 * setup the projection and swap x and z axis
	 */
	protected void doRenderInHand( ItemStack stack, MetaItem meta ) { this.render( meta ); }
}
