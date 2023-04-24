package com.mcwb.common;

import org.apache.logging.log4j.Logger;

import net.minecraft.client.resources.I18n;
import net.minecraftforge.fml.relauncher.Side;

/**
 * Implement this interface if you need the logger.
 * 
 * @author Giant_Salted_Fish
 */
public interface IAutowireLogger
{
	default Logger logger() { return MCWB.LOGGER; }
	
	/**
	 * You can call this to translate your message if your code runs on both side. Otherwise,
	 * simply use {@link I18n} if your code only runs on {@link Side#CLIENT} side.
	 * 
	 * @see I18n#format(String, Object...)
	 */
	default String format( String translateKey, Object... parameters ) {
		return MCWB.MOD.format( translateKey, parameters );
	}
	
	/// *** Wrap commonly used calls. *** ///
	default void logInfo( String translateKey, Object... parameters ) {
		this.logger().info( this.format( translateKey, parameters ) );
	}
	
	default void logWarning( String translateKey, Object... parameters ) {
		this.logger().warn( this.format( translateKey, parameters ) );
	}
	
	default void logError( String translateKey, Object... parameters ) {
		this.logger().error( this.format( translateKey, parameters ) );
	}
	
	default void logException( Throwable e, String translateKey, Object... parameters ) {
		this.logger().error( this.format( translateKey, parameters ), e );
	}
}
