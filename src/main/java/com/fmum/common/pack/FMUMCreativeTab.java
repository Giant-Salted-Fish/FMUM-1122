package com.fmum.common.pack;

import java.util.HashMap;

import com.fmum.common.FMUM;
import com.fmum.common.type.TypeInfo;
import com.fmum.common.type.TypeTextParser.LocalTypeFileParser;

import net.minecraft.client.resources.I18n;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Super class of the creative tabs that services for {@link FMUM} content packs
 * 
 * @see IconBasedTab
 * @author Giant_Salted_Fish
 */
public class FMUMCreativeTab extends CreativeTabs
{
	public static final HashMap<String, FMUMCreativeTab> tabs = new HashMap<>();
	
	public static final String RECOMMENDED_SOURCE_DIR_NAME = "tab";
	
	public static final ItemStack DEF_ICON_STACK = new ItemStack(Blocks.WOOL, 1, 10);
	
	/**
	 * Default creative item tab for {@link FMUM}
	 */
	public static final FMUMCreativeTab INSTANCE = new FMUMCreativeTab(FMUM.MODID, FMUM.MOD_NAME);
	
	public FMUMCreativeTab(String label, String contentPackName)
	{
		super(label);
		
		tabs.put(label, this);
	}
	
	/**
	 * Called when an item requires to settle in this tab. Can be used to implement classical Flan's
	 * Mod creative tab.
	 * 
	 * @param item Item wants to settle in
	 */
	public void itemSettleIn(Item item) { }
	
	@SideOnly(Side.CLIENT)
	public void setupIconStack() { }
	
	@Override
    @SideOnly(Side.CLIENT)
	public ItemStack createIcon() { return DEF_ICON_STACK; }
	
	@Override
	public String toString() { return "tab:" + this.getTabLabel(); }
	
	/**
	 * A simple implementation of {@link FMUMCreativeTab}. It specifies a static item to be its tab
	 * icon.
	 * 
	 * @author Giant_Salted_Fish
	 */
	public static class IconBasedTab extends FMUMCreativeTab
	{
		public static final LocalTypeFileParser<IconBasedTab>
			parser = new LocalTypeFileParser<>(IconBasedTab.class, null);
		static
		{
			parser.addKeyword(
				"IconItem",
				(s, t) -> {
					t.iconItemName = s[1];
					if(s.length > 2)
						t.iconItemDam = Integer.parseInt(s[2]);
				}
			);
		}
		
		public static final String DEF_ICON_ITEM_NAME = "undefined";
		
		public String iconItemName = DEF_ICON_ITEM_NAME;
		public int iconItemDam = 0;
		
		public ItemStack iconStack = DEF_ICON_STACK;
		
		public IconBasedTab(String label, String contentPackName) { super(label, contentPackName); }
		
		@Override
		@SideOnly(Side.CLIENT)
		public void setupIconStack()
		{
			if(this.iconItemName == DEF_ICON_ITEM_NAME) return;
			
			// Check if it requires an item defined in FMUM
			TypeInfo type = TypeInfo.types.get(this.iconItemName);
			if(type == null)
			{
				Item item = Item.getByNameOrId(this.iconItemName);
				if(item == null)
					FMUM.log.error(
						I18n.format(
							"fmum.failtofindiconitemfortab",
							this.iconItemName,
							this.getTabLabel()
						)
					);
				else this.iconStack = new ItemStack(item, 1, this.iconItemDam);
			}
			else this.iconStack = new ItemStack(type.item, 1, this.iconItemDam);
		}
		
		@Override
	    @SideOnly(Side.CLIENT)
		public ItemStack createIcon() { return this.iconStack; }
	}
}