package com.fmum.client.render;

import com.fmum.client.EventHandlerClient;
import com.fmum.client.FMUMClient;
import com.fmum.client.Operation;

import net.minecraft.util.ResourceLocation;

public interface Renderable
{
	/**
	 * Preliminary render method without any additional render information provided, hence it can be
	 * called in any context. It is recommended to simply render your model in this method to ensure
	 * the compatibility.
	 */
	public default void render() { }
	
	/**
	 * <p> Called when the corresponding model is focused or high lighted. In default it glows the
	 * model before rendering it. </p>
	 * 
	 * <p> Notice that calling glow twice could cause unexpected light problem. Hence it is
	 * recommended to override this method if you used {@link #glowOn()} or {@link #glowOn(int)}
	 * </p>
	 */
	public default void renderHighLighted()
	{
		this.glowOn();
		this.render();
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
