package com.fmum.common.type;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import com.fmum.common.ForgeEventListener;
import com.fmum.common.ForgeEventListener.RequireItemRegistration;
import com.fmum.common.pack.FMUMCreativeTab;
import com.fmum.common.type.TypeTextParser.LocalTypeFileParser;

public abstract class TypeInfo implements RequireItemRegistration
{
	/**
	 * A map that maps all info types with their name
	 */
	public static final HashMap<String, TypeInfo> types = new HashMap<>();
	
	/**
	 * Default item description
	 */
	public static final LinkedList<String> DEF_DESCRIPTION = new LinkedList<>();
	
	public static final LocalTypeFileParser<TypeInfo>
		parser = new LocalTypeFileParser<>(null, TypeInfo.class);
	static
	{
		parser.addKeyword("Name", (s, t) -> t.name = s[1]);
		parser.addKeyword(
			"Description",
			(s, t) -> {
				if(t.description == DEF_DESCRIPTION)
					t.description = new LinkedList<>();
				String des = s[s.length - 1];
				for(int i = s.length - 1; --i > 0; des = s[i] + " " + des);
				t.description.add(des);
			}
		);
	}
	
	/**
	 * Identifier of this item
	 */
	public String name;
	
	/**
	 * Name of the content pack where this item belongs to
	 */
	public final String contentPackName;
	
	/**
	 * Description that will be displayed when player hovers over the item
	 */
	public List<String> description = DEF_DESCRIPTION;
	
	/**
	 * Creative tab where this item will be displayed in
	 */
	public String creativeTab = FMUMCreativeTab.INSTANCE.getTabLabel();
	
	public TypeInfo(String name, String contentPackName)
	{
		this.name = name;
		this.contentPackName = contentPackName;
	}
	
	/**
	 * <p>Called after parsing this typer from a plain text file. In default this method put this
	 * instance into {@link #types} and add itself into
	 * {@link ForgeEventListener#itemsWaitForRegistration}.</p>
	 * 
	 * <p>Notice that this method will not be called for class based typer. Hence you should
	 * call this method or do similar things yourself in your typer constructor.</p>
	 */
	public void postParse()
	{
		types.put(this.name, this);
		ForgeEventListener.itemsWaitForRegistration.add(this);
	}
	
	/**
	 * @return {@link EnumType} that this instance belongs to
	 */
	public abstract EnumType getEnumType();
	
	@Override
	public String toString() {
		return "<" + this.getEnumType() + ">" + this.contentPackName + ":" + this.name;
	}
}
