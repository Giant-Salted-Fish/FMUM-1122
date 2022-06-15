package com.fmum.common.type;

import java.util.HashMap;
import java.util.TreeMap;

import com.fmum.client.ClientProxy;
import com.fmum.client.ResourceManager;
import com.fmum.common.FMUM;
import com.fmum.common.pack.FMUMContentProvider;
import com.fmum.common.type.TypeTextParser.LocalTypeFileParser;
import com.fmum.common.util.Util;

import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

/**
 * Provides information for a variant of an item. It can be used as the Paintjob class in Flan's Mod
 * and provides an universal abstraction on {@link TypeInfo} between Paintjob.
 * 
 * @author Giant_Salted_Fish
 */
public class ItemVariant implements Comparable<ItemVariant>
{
	public static final HashMap<String, ItemVariant> variants = new HashMap<>();
	
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
					FMUM.proxy.addLocalizeKey(t.translationKey + ".name", Util.splice(s, 2));
			}
		);
		parser.addKeyword("Texture", (s, t) -> t.texture = s[1]);
		parser.addKeyword("Material", (s, t) -> t.material = parseMaterial(s, 1));
	}
	
	public static final String
		TRANSLATION_PREFIX = "item.",
		TRANSLATION_SUFFIX = ".name";
	
	/**
	 * A parser that reads attributes for item variant. Note that it will not set {@link #name} and
	 * {@link #translationKey} cause they are initialized in instantiation.
	 */
	protected static final ParserFunc<ItemVariant> paintjobParser = (s, t) -> {
		t.texture = s.length > 2 ? s[2] : ClientProxy.RECOMMENDED_TEXTURE_FOLDER + s[1] + ".png";
		if(s.length > 3)
			t.material = parseMaterial(s, 3);
	};
	
	protected static final TreeMap<String, Integer> DEF_MATERIAL = new TreeMap<>();
	
	/**
	 * Name of the content pack where this item belongs to
	 */
	public FMUMContentProvider provider = null;
	
	/**
	 * Identifier of this item
	 */
	public String name;
	
	/**
	 * Translation key for localization. In default is {@value #TRANSLATION_PREFIX} + {@link #name}.
	 */
	public String translationKey;
	
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
	
	public ItemVariant notifyProvider(FMUMContentProvider provider)
	{
		this.provider = provider;
		return this;
	}
	
	/**
	 * Called after parsing this typer from a plain text file
	 */
	public void postParse()
	{
		if(this.texture == null)
			this.texture = ClientProxy.RECOMMENDED_TEXTURE_FOLDER + this.name + ".png";
		
		variants.put(this.name, this);
	}
	
	/**
	 * Called right after all types been loaded
	 */
	public void postLoad() { }
	
	public ResourceLocation getTexture(ItemStack stack) {
		return ResourceManager.getTexture(this.texture);
	}
	
	/**
	 * @return {@link EnumType} that this instance belongs to
	 */
	public EnumType getEnumType() { return null; }
	
	@Override
	public int compareTo(ItemVariant v) { return this.name.compareTo(v.name); }
	
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
