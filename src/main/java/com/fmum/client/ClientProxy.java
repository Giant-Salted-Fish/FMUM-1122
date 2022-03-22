package com.fmum.client;

import java.io.File;
import java.util.HashMap;

import com.fmum.common.CommonProxy;
import com.fmum.common.FMUM;
import com.fmum.common.pack.FMUMCreativeTab;

import net.minecraft.client.resources.I18n;
import net.minecraftforge.client.resource.VanillaResourceType;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.FMLModContainer;
import net.minecraftforge.fml.common.MetadataCollection;
import net.minecraftforge.fml.common.discovery.ContainerType;
import net.minecraftforge.fml.common.discovery.ModCandidate;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public final class ClientProxy extends CommonProxy
{
	public static final String RECOMMENDED_TEXTURE_FOLDER = "skins" + File.separator;
	
	@Override
	public void syncConfig(Configuration config)
	{
		this.parseConfig(config);
		
		// TODO Parse client side only settings
		
		// Save configuration file if has changed
		if(config.hasChanged())
			config.save();
	}
	
	@Override
	public void loadLocalizationMap() { }
	
	@Override
	public void registerEventListeners()
	{
		super.registerEventListeners();
		
		MinecraftForge.EVENT_BUS.register(ForgeEventListenerClient.class);
	}
	
	@Override
	public String format(String translateKey, Object... parameters) {
		return I18n.format(translateKey, parameters);
	}
	
	@Override
	public String addLocalizeKey(String key, String formator) { return null; }
	
	@Override
	public void registerLocalResource(File source)
	{
		super.registerLocalResource(source);
		
		// Register it as a resource pack to load textures and sounds client side
		HashMap<String, Object> descriptor = new HashMap<>();
		descriptor.put("modid", FMUM.MODID);
		descriptor.put("name", FMUM.MOD_NAME + ":" + source.getName());
		descriptor.put("version", "1");
		FMLModContainer container = new FMLModContainer(
			"com.fmum.common.FMUM",
			new ModCandidate(
				source,
				source,
				source.isDirectory() ? ContainerType.DIR : ContainerType.JAR
			),
			descriptor
		);
		container.bindMetadata(MetadataCollection.from(null, ""));
		FMLClientHandler.instance().addModAsResource(container);
	}
	
	@Override
	public void refreshMinecraftResources()
	{
		FMLClientHandler.instance().refreshResources(
			VanillaResourceType.MODELS,
			VanillaResourceType.TEXTURES,
			VanillaResourceType.SOUNDS,
			VanillaResourceType.LANGUAGES
		);
	}
	
	@Override
	public void setupCreativeTabs()
	{
		for(FMUMCreativeTab tab : FMUMCreativeTab.tabs.values())
			tab.setupIconStack();
	}
}