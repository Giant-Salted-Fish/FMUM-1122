package com.fmum.client.render;

import com.fmum.client.EventHandlerClient;
import com.fmum.client.FMUMClient;
import com.fmum.client.Operation;
import com.fmum.common.meta.MetaBase;

import net.minecraft.util.ResourceLocation;

public interface RenderableBase
{
	/**
	 * Preliminary render method without any additional render information provided, hence it can be
	 * called in any context. In most cases it only renders the meshes.
	 */
	public default void render() { }
	
	/**
	 * Simply bind texture and render mesh
	 * 
	 * @param
	 *     meta Corresponding meta to provide extra info needed for rendering. For example the
	 *     texture.
	 */
	public default void render( MetaBase meta ) { }
	
	/**
	 * <p> Called when the corresponding model is focused or high lighted. In default it glows the
	 * model before rendering it. </p>
	 * 
	 * <p> Notice that calling glow twice could cause unexpected lighting problem. Hence it is
	 * recommended to override this method if you used {@link #glowOn()} or {@link #glowOn(int)}
	 * in {@link #render(MetaBase)} or {@link #render()} </p>
	 */
	public default void renderHighLighted( MetaBase meta )
	{
		this.glowOn();
		this.render( meta );
		this.glowOff();
	}
	
	/**
	 * @return Partial tick time
	 */
	public default float smoother() { return EventHandlerClient.renderTickTime; }
	
	public default Operation operating() { return FMUMClient.MOD.operating; }
	
	public default void bindTexture( ResourceLocation texture ) {
		FMUMClient.mc.renderEngine.bindTexture( texture );
	}
	
	/**
	 * Call {@link #glowOn(int)} with glow level 15
	 */
	public default void glowOn() { this.glowOn( 15 ); }
	
	/**
	 * The model rendered after this call will glow until {@link #glowOff()} is called
	 * 
	 * @param glow level of glow range from 0-15
	 */
	public void glowOn( int glow );
	
	public void glowOff();
}
