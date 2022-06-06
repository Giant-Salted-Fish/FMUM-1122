package com.fmum.common.meta;

import com.fmum.client.input.TypeKeyBind;
import com.fmum.client.input.TypeSpKeyBind;
import com.fmum.common.FMUM;
import com.fmum.common.pack.TypeCreativeTab;
import com.fmum.common.paintjob.TypeExternalPaintjob;
import com.fmum.common.util.LocalAttrParser;
import com.fmum.common.weapon.gun.TypeGun;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Meta types that are supported by {@link FMUM} framework(leaf derived class of {@link MetaBase})
 * 
 * @author Giant_Salted_Fish
 */
public enum EnumMeta
{
	/**
	 * If not specified, this will be the default. For third party extension.
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
	 * @see TODO
	 */
	ATTACHMENT( "attachment", null ),
	
	/**
	 * Gun magazines
	 * 
	 * @see TODO
	 */
	MAG( "mag", null ),
	
	/**
	 * External paintjobs that will be inject into {@link TypePaintable} in post load phase
	 * 
	 * @see TypeExternalPaintjob
	 */
	EX_PAINTJOB( "expaintjob", null ),
	
	/**
	 * Key binds that specified by content pack
	 * 
	 * @see TypeKeyBind
	 */
	@SideOnly( Side.CLIENT )
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
