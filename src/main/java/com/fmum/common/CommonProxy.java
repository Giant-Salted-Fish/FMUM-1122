package com.fmum.common;

import java.io.File;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.TreeMap;
import java.util.regex.Pattern;

import com.fmum.common.pack.FMUMContentProvider;
import com.fmum.common.pack.FolderContentPack;
import com.fmum.common.pack.ZipContentPack;

import net.minecraft.client.resources.I18n;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class CommonProxy
{
	/**
	 * All content packs loaded for {@link FMUM}
	 */
	public static final TreeMap<String, FMUMContentProvider> contentProviders = new TreeMap<>();
	
	protected static final Pattern SUFFIX_ZIP_JAR = Pattern.compile("(.+)\\.(zip|jar)$");
	
	/**
	 * Name of the folder that contains content packs to be loaded
	 */
	public static String packDirName = FMUM.MODID;
	
	@SideOnly(Side.SERVER)
	private static String localizeFileName;
	
	/**
	 * For server side localization
	 */
	@SideOnly(Side.SERVER)
	private static HashMap<String, String> localizationMap;
	
	public void syncConfig(Configuration config)
	{
		localizeFileName = config.getString(
			"localizationFile",
			"Server Only",
			"en_us.lang",
			"Server side localization file"
		);
		
		this.parseConfig(config);
		
		// Save configuration file if has changed
		if(config.hasChanged())
			config.save();
	}
	
	public void loadLocalizationMap()
	{
		// TODO Add server side localization
		final HashMap<String, String> m = localizationMap = new HashMap<>();
	}
	
	public void registerEventListeners()
	{
		MinecraftForge.EVENT_BUS.register(ForgeEventListener.class);
	}
	
	/**
	 * This localize the message based on the physical game side. Use this for localization if the
	 * message could be print in both side.
	 * 
	 * @see I18n#format(String, Object...)
	 */
	public String format(String translateKey, Object... parameters)
	{
		String format = localizationMap.get(translateKey);
		return String.format(format != null ? format : translateKey, parameters);
	}
	
	public String addLocalizeKey(String key, String formator) {
		return localizationMap.put(key, formator);
	}
	
	public final void loadContentPack(File mcDir)
	{
		// Check content pack folder
		File packDir = new File(mcDir, packDirName);
		if(!packDir.exists())
		{
			packDir.mkdirs();
			FMUM.log.info(this.format("fmum.packfoldercreated", packDirName));
		}
		
		// Find all content providers
		final LinkedList<FMUMContentProvider> providers = new LinkedList<>();
		for(File file : packDir.listFiles())
		{
			FMUMContentProvider provider;
			if(file.isDirectory())
				provider = new FolderContentPack(file);
			else if(SUFFIX_ZIP_JAR.matcher(file.getName()).matches())
				provider = new ZipContentPack(file);
			else
			{
				FMUM.log.warn(
					this.format(
						"fmum.unrecognizedcontentpackfiletype",
						packDir.getName() + "/" + file.getName()
					)
				);
				continue;
			}
			
			providers.add(provider);
			FMUM.log.info(this.format("fmum.detectcontentpack", provider.getSourceName()));
		}
		
		// Load packs!
		for(FMUMContentProvider fcm : providers)
		{
			fcm.prepareLoad();
			FMUM.log.info(this.format("fmum.preloadcontentpack", fcm.getSourceName()));
		}
		for(FMUMContentProvider fcm : providers)
		{
			fcm.loadContents();
			contentProviders.put(fcm.getName(), fcm);
			FMUM.log.info(this.format("fmum.loadedcontentpack", fcm.getSourceName()));
		}
	}
	
	public void registerLocalResource(File source)
	{
		try { FMUMClassLoader.instance.addURL(source.toURI().toURL()); }
		catch(MalformedURLException e) {
			FMUM.log.error(this.format("fmum.erroraddingclasspath", source.getName()), e);
		}
	}
	
	/**
	 * Reload Minecraft resources client side to load resource in content packs
	 */
	public void refreshMinecraftResources() { }
	
	/**
	 * Called on client side to prepare tab icon item
	 */
	public void setupCreativeTabs() { }
	
	public final void initComplete()
	{
		FMUM.log.info(
			this.format(
				"fmum.infoactivedpacks",
				Integer.toString(contentProviders.size())
			)
		);
		for(FMUMContentProvider pack : contentProviders.values())
			FMUM.log.info(
				this.format(
					"fmum.activedpackinfo",
					this.format(pack.getName()),
					this.format(pack.getAuthor())
				)
			);
	}
	
	protected final void parseConfig(Configuration config)
	{
		final String COMMON_SETTING = "Common";
		
		packDirName = config.getString(
			"packFolderName",
			COMMON_SETTING,
			packDirName,
			"Content pack folder name where FMUM will load content packs from"
		);
	}
}
