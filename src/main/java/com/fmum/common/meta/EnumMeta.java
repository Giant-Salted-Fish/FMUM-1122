package com.fmum.common.meta;

import com.fmum.client.input.TypeKeyBind;
import com.fmum.client.input.TypeSpKeyBind;
import com.fmum.common.FMUM;
import com.fmum.common.gun.TypeGun;
import com.fmum.common.pack.TypeCreativeTab;
import com.fmum.common.paintjob.TypeExternalPaintjob;
import com.fmum.common.util.LocalAttrParser;

/**
 * Meta types that are supported by {@link FMUM} framework(leaf derived class of {@link MetaBase})
 * 
 * @author Giant_Salted_Fish
 */
public enum EnumMeta
{
	/**
	 * If not specified, this will be the default. For third party extension use.
	 * 
	 * @see TypeMeta
	 */
	GENERAL( "general", null ),
	
	/**
	 * Creative tabs
	 * 
	 * @see TypeCreativeTab
	 */
	TAB( "tab", TypeCreativeTab.parser ),
	
	/**
	 * Guns
	 * 
	 * @see TypeGun
	 */
	GUN( "gun", TypeGun.parser ),
	
	/**
	 * Gun attachments
	 * 
	 * @see TODO: add attachment parser
	 */
//	ATTACHMENT( "attachment", null ),
	
	/**
	 * Gun magazines
	 * 
	 * @see TODO: add magazine parser
	 */
//	MAG( "mag", null ),
	
	/**
	 * External paintjobs that will be inject into {@link TypePaintable} in post load phase
	 * 
	 * @see TypeExternalPaintjob
	 */
	EX_PAINTJOB( "expaintjob", TypeExternalPaintjob.parser ),
	
	/**
	 * Key binds that specified by content pack
	 * 
	 * @see TypeKeyBind
	 */
	KEY_BIND( "keybind", TypeSpKeyBind.parser );
	
	public final String recommendedSourceDirName;
		
	public final LocalAttrParser< ? extends MetaBase > parser;
	
	private EnumMeta(
		String recommendedSourceDirName,
		LocalAttrParser< ? extends MetaBase > parser
	) {
		this.recommendedSourceDirName = recommendedSourceDirName;
		this.parser = parser;
	}
}
