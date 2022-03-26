package com.fmum.common.type;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import com.fmum.client.model.Model;
import com.fmum.client.model.ModelDebugBox;
import com.fmum.common.CommonProxy;
import com.fmum.common.EventHandler;
import com.fmum.common.EventHandler.RequireItemRegister;
import com.fmum.common.FMUM;
import com.fmum.common.pack.FMUMContentProvider;
import com.fmum.common.pack.FMUMCreativeTab;
import com.fmum.common.type.TypeTextParser.LocalTypeFileParser;

import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.registries.IForgeRegistry;

public abstract class TypeInfo extends ItemVariant implements RequireItemRegister
{
	/**
	 * A map that maps all info types with their name
	 */
	public static final HashMap<String, TypeInfo> types = new HashMap<>();
	
	public static final LocalTypeFileParser<TypeInfo>
		parser = new LocalTypeFileParser<>(ItemVariant.parser);
	static
	{
		parser.addKeyword("Category", (s, t) -> t.category = s[1]);
		parser.addKeyword(
			"Description",
			(s, t) -> {
				if(t.description == TypeInfo.DEF_DESCRIPTION)
					t.description = new LinkedList<>();
				t.description.add(FMUM.splice(s, 1));
			}
		);
		parser.addKeyword("CreativeTab", (s, t) -> t.creativeTab = s[1]);
		
		// Visual
		parser.addKeyword(
			"Model",
			(s, t) -> {
				switch(s.length)
				{
				case 4: t.texture = s[3];
				case 3: t.modelScale = Float.parseFloat(s[2]);
				default: t.modelPath = s[1];
				}
			}
		);
	}
	
	public static final String MODEL_RES_INV = "inventory";
	
	/**
	 * Default item description
	 */
	protected static final LinkedList<String> DEF_DESCRIPTION = new LinkedList<>();
	static { DEF_DESCRIPTION.add("tooltip.descriptionmissing"); }
	
	/**
	 * Minecraft item that corresponding to this typer. Usually set on item registration.
	 */
	public Item item = null;
	
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
	 * Path of the model. Will be used to load {@link #model}.
	 */
	public String modelPath = ModelDebugBox.PATH + ":box";
	
	/**
	 * Corresponding 3D model of this item. Due to some reasons this will be loaded on first time
	 * that the player enters the world.
	 */
	public Model model = null;
	
	/**
	 * Scale that should be applied when rendering this model
	 */
	public float modelScale = 1F;
	
	protected TypeInfo(String name) { super(name); }
	
	public TypeInfo noticeProvider(FMUMContentProvider provider)
	{
		this.provider = provider;
		return this;
	}
	
	/**
	 * <p>Called after parsing this typer from a plain text file. In default this method puts this
	 * instance into {@link #types} and register itself into
	 * {@link CommonProxy#typesWaitForPostLoadProcess}.
	 * 
	 * <p>Notice that this method will not be called for class based typer. Hence you should call
	 * this method or do similar things yourself in your typer constructor.</p>
	 */
	@Override
	public void postParse()
	{
		super.postParse();
		
		types.put(this.name, this);
		CommonProxy.typesWaitForPostLoadProcess.add(this);
	}
	
	/**
	 * Called right after all types been loaded. In default it calls {@link #onItemSetup()} and adds
	 * itself into {@link EventHandler#itemsWaitForRegistration}.
	 */
	public void postLoad()
	{
		this.onItemSetup();
		EventHandler.itemsWaitForRegistration.add(this);
	}
	
	@Override
	public void onItemRegister(IForgeRegistry<Item> registry) { registry.register(this.item); }
	
	@Override
	public void onModelRegister(ModelRegistryEvent evt)
	{
		ModelLoader.setCustomModelResourceLocation(
			this.item,
			0, // modid + name
			new ModelResourceLocation(this.item.getRegistryName(), MODEL_RES_INV)
		);
	}
	
	/**
	 * @return {@link EnumType} that this instance belongs to
	 */
	public abstract EnumType getEnumType();
	
	public String getDisplayName(ItemStack stack) {
		return FMUM.proxy.format(this.item.getTranslationKey(stack) + TRANSLATION_SUFFIX);
	}
	
	/**
	 * Called when the first the player enters a world to load models
	 */
	public void loadModel() { this.model = FMUM.proxy.loadModel(this.modelPath); }
	
	@Override
	public String toString() {
		return "<" + this.getEnumType() + ">" + this.provider.getName() + ":" + this.name;
	}
	
	protected abstract void onItemSetup();
	
	/**
	 * Call {@link #withItem(Item, int, int)} with parameter list {@code (item, 1, 0)}
	 */
	protected final void withItem(Item item) { this.withItem(item, 1, 0); }
	
	/**
	 * Helper method that initializes the given item and binds it to this typer
	 * 
	 * @param item Item to bind
	 * @param maxStackSize Max stack size of this item
	 * @param maxDamage Max item of this item
	 * @return Parameter {@code item}
	 */
	protected final void withItem(Item item, int maxStackSize, int maxDamage)
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
	}
}
