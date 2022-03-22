package com.fmum.common.type;

import java.util.TreeMap;

import com.fmum.client.ClientProxy;
import com.fmum.common.FMUM;
import com.fmum.common.type.TypeTextParser.LocalTypeFileParser;

/**
 * Provides information for a variant of an item. It can be used as the Paintjob class in Flan's Mod
 * and provides an universal abstraction on {@link TypeInfo} between Paintjob.
 * 
 * @author Giant_Salted_Fish
 */
public class ItemVariant
{
	public static final LocalTypeFileParser<ItemVariant> parser = new LocalTypeFileParser<>(null);
	static
	{
		parser.addKeyword(
			"Name",
			(s, t) -> {
				t.name = s[1];
				t.translationKey = ItemVariant.TRANSLATION_PREFIX + t.name;
				
				// Set a server side localized name
				if(s.length > 2)
					FMUM.proxy.addLocalizeKey(t.translationKey + ".name", FMUM.splice(s, 2));
			}
		);
		parser.addKeyword("Icon", (s, t) -> t.iconPath = s[1]);
		parser.addKeyword("Texture", (s, t) -> t.texture = s[1]);
		parser.addKeyword("Material", (s, t) -> t.material = parseMaterial(s, 1));
	}
	
	public static final String
		TRANSLATION_PREFIX = "item.",
		TRANSLATION_SUFFIX = ".name";
	
	protected static final ParserFunc<ItemVariant> paintjobParser = (s, t) -> {
		t.translationKey = TRANSLATION_PREFIX + s[1];
		t.iconPath = s[s.length > 2 ? 2 : 1];
		t.texture = s[s.length > 3 ? 3 : 1];
		if(s.length > 4)
			t.material = parseMaterial(s, 4);
	};
	
	protected static final TreeMap<String, Integer> DEF_MATERIAL = new TreeMap<>();
	
	/**
	 * Identifier of this item
	 */
	public String name;
	
	/**
	 * Translation key for localization. In default is {@value #TRANSLATION_PREFIX} + {@link #name}.
	 */
	public String translationKey;
	
	/**
	 * Path of the icon for this item
	 */
	public String iconPath = null;
	
	/**
	 * Texture of the item. Usually for 3D model rendering.
	 */
	public String texture = null;
	
	/**
	 * Material required to transfer into this variant
	 */
	public TreeMap<String, Integer> material = DEF_MATERIAL;
	
	public ItemVariant(String name)
	{
		this.name = name;
		this.translationKey = TRANSLATION_PREFIX + name;
	}
	
	public void postParse()
	{
		if(this.iconPath == null) this.iconPath = this.name;
		if(this.texture == null) this.texture = ClientProxy.RECOMMENDED_TEXTURE_FOLDER + this.name;
	}
	
	protected static TreeMap<String, Integer> parseMaterial(String[] split, int cursor)
	{
		final TreeMap<String, Integer> ret = new TreeMap<>();
		while(cursor < split.length)
		{
			ret.put(
				split[cursor],
				cursor + 1 < split.length ? Integer.parseInt(split[cursor + 1]) : 1
			);
			cursor += 2;
		}
		return ret;
	}
}
