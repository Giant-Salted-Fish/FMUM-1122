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
	 * Creative tabs
	 * 
	 * @see TypeCreativeTab
	 */
	TAB( "tab", FMUM.MOD.isClient() ? TypeCreativeTab.parserClient : TypeCreativeTab.parser ),
	
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
	KEY_BIND(
		// Set folder name to something that will never use if it is on server side to skip key load
		FMUM.MOD.isClient() ? "keybind" : "client",
		
		// TypeSpKeyBind is side only so avoid it if it is on server side
		FMUM.MOD.isClient() ? TypeSpKeyBind.parserClient : null
	),
	
	/**
	 * For third party extension. No corresponding text file type parser. Only class based type
	 * supported.
	 * 
	 * @see TypeBase
	 */
	OTHER( "other", null );
	
	public final String dirName;
	
	public final LocalAttrParser< ? extends MetaBase > parser;
	
	private EnumMeta( String dirName, LocalAttrParser< ? extends MetaBase > parser )
	{
		this.dirName = dirName;
		this.parser = parser;
	}
}
