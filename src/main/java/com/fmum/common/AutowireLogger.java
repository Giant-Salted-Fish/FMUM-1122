package com.fmum.common;

import org.apache.logging.log4j.Logger;

import net.minecraft.client.resources.I18n;
import net.minecraftforge.fml.relauncher.Side;

/**
 * Provide logger that used for logging in {@link FMUM}
 * 
 * @author Giant_Salted_Fish
 */
public interface AutowireLogger
{
	public default Logger log() { return FMUM.log; }
	
	/**
	 * You can call this to translate your message if the code will run on both side. Otherwise,
	 * simply use {@link I18n} if your code only runs on {@link Side#CLIENT} side.
	 * 
	 * @see I18n#format(String, Object...)
	 */
	public default String format( String translateKey, Object... parameters ) {
		return FMUM.MOD.format( translateKey, parameters );
	}
}
