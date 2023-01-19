package com.mcwb.common;

import org.apache.logging.log4j.Logger;

import net.minecraft.client.resources.I18n;
import net.minecraftforge.fml.relauncher.Side;

public interface IAutowireLogger
{
	public default Logger logger() { return MCWB.LOGGER; }
	
	/**
	 * You can call this to translate your message if the code may run on both side. Otherwise,
	 * simply use {@link I18n} if your code only runs on {@link Side#CLIENT} side.
	 * 
	 * @see I18n#format(String, Object...)
	 */
	public default String format( String translateKey, Object... parameters ) {
		return MCWB.MOD.format( translateKey, parameters );
	}
	
	/// Convenient packed methods that are commonly used ///
	public default void info( String translateKey, Object... parameters ) {
		this.logger().info( this.format( translateKey, parameters ) );
	}
	
	public default void warn( String translateKey, Object... parameters ) {
		this.logger().warn( this.format( translateKey, parameters ) );
	}
	
	public default void error( String translateKey, Object... parameters ) {
		this.logger().error( this.format( translateKey, parameters ) );
	}
	
	public default void except( Throwable e, String translateKey, Object... parameters ) {
		this.logger().error( this.format( translateKey, parameters ), e );
	}
}
