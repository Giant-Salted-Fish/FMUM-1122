package com.fmum.common.type;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import com.fmum.common.FMUM;
import com.fmum.common.ForgeEventListener;
import com.fmum.common.ForgeEventListener.RequireItemRegistration;
import com.fmum.common.pack.FMUMContentProvider;
import com.fmum.common.pack.FMUMCreativeTab;
import com.fmum.common.type.TypeTextParser.LocalTypeFileParser;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

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
	static { DEF_DESCRIPTION.add("tooltip.descriptionmissing"); }
	
	public static final LocalTypeFileParser<TypeInfo>
		parser = new LocalTypeFileParser<>(null);
	static
	{
		parser.addKeyword(
			"Name",
			(s, t) -> {
				t.name = s[1];
				
				// Set a server side localized name
				if(s.length > 2)
					FMUM.proxy.addLocalizeKey("item." + t.name + ".name", FMUM.splice(s, 2));
			}
		);
		parser.addKeyword("Category", (s, t) -> t.category = s[1]);
		parser.addKeyword(
			"Description",
			(s, t) -> {
				if(t.description == DEF_DESCRIPTION)
					t.description = new LinkedList<>();
				t.description.add(FMUM.splice(s, 1));
			}
		);
		parser.addKeyword("CreativeTab", (s, t) -> t.creativeTab = s[1]);
		
		// Visual
		parser.addKeyword("Icon", (s, t) -> t.iconPath = s[1]);
	}
	
	/**
	 * Minecraft item that corresponding to this typer. Usually set on item registration.
	 */
	public Item item = null;
	
	/**
	 * Identifier of this item
	 */
	public String name;
	
	/**
	 * Name of the content pack where this item belongs to
	 */
	public FMUMContentProvider provider = null;
	
	/**
	 * Category of this item. Usually used in grouping different items.
	 */
	public String category = "default";
	
	/**
	 * Description that will be displayed when player hovers over the item
	 */
	public List<String> description = DEF_DESCRIPTION;
	
	/**
	 * Creative tab where this item will be displayed in
	 */
	public String creativeTab = FMUMCreativeTab.INSTANCE.getTabLabel();
	
	/**
	 * Path of the icon for this item
	 */
	public String iconPath = "undefined";
	
	/**
	 * Scale that should be applied when rendering this model
	 */
	public float modelScale = 1F;
	
	protected TypeInfo(String name) { this.name = name; }
	
	public TypeInfo noticeProvider(FMUMContentProvider provider)
	{
		this.provider = provider;
		return this;
	}
	
	/**
	 * <p>Called after parsing this typer from a plain text file. In default this method puts this
	 * instance into {@link #types} and adds itself into
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
	
	public String getDisplayName(ItemStack stack) {
		return FMUM.proxy.format(this.item.getTranslationKey(stack) + ".name");
	}
	
	@Override
	public String toString() {
		return "<" + this.getEnumType() + ">" + this.provider.getName() + ":" + this.name;
	}
	
	protected final Item withItem(Item item) { return this.withItem(item, 1, 0); }
	
	protected final Item withItem(Item item, int maxStackSize, int maxDamage)
	{
		this.item = item;
		item.setRegistryName(this.name);
		item.setTranslationKey(this.name);
		item.setMaxStackSize(maxStackSize);
		item.setMaxDamage(maxDamage);
		
		// Try set required creative tab
		FMUMCreativeTab tab = FMUMCreativeTab.tabs.get(this.creativeTab);
		if(tab == null)
		{
			FMUM.log.error(
				FMUM.proxy.format(
					"fmum.failtofetchcreativetab",
					this.creativeTab,
					this.toString()
				)
			);
			tab = FMUMCreativeTab.INSTANCE;
		}
		item.setCreativeTab(tab);
		tab.itemSettleIn(item);
		
		return item;
	}
}
