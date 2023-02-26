package com.mcwb.common;

import org.apache.logging.log4j.Logger;

import net.minecraft.client.resources.I18n;
import net.minecraftforge.fml.relauncher.Side;

/**
 * Implement this interface if you need the logger
 * 
 * @author Giant_Salted_Fish
 */
public interface IAutowireLogger
{
	public default Logger logger() { return MCWB.LOGGER; }
	
	/**
	 * You can call this to translate your message if your code runs on both side. Otherwise,
	 * simply use {@link I18n} if your code only runs on {@link Side#CLIENT} side.
	 * 
	 * @see I18n#format(String, Object...)
	 */
	public default String format( String translateKey, Object... parameters ) {
		return MCWB.MOD.format( translateKey, parameters );
	}
	
	/// *** Wrap commonly used calls *** ///
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
