package com.fmum.common;

import java.io.File;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.TreeMap;
import java.util.regex.Pattern;

import com.fmum.client.model.Model;
import com.fmum.common.pack.FMUMContentProvider;
import com.fmum.common.pack.FolderContentPack;
import com.fmum.common.pack.ZipContentPack;
import com.fmum.common.type.TypeInfo;

import net.minecraft.client.resources.I18n;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class CommonProxy
{
	/**
	 * All content packs loaded for {@link FMUM}
	 */
	public static final TreeMap<String, FMUMContentProvider> contentProviders = new TreeMap<>();
	
	public static final LinkedList<TypeInfo> typesWaitForPostLoadProcess = new LinkedList<>();
	
	protected static final Pattern SUFFIX_ZIP_JAR = Pattern.compile("(.+)\\.(zip|jar)$");
	
	protected static File mcDir = null;
	
	/**
	 * Name of the folder that contains content packs to be loaded
	 */
	public static String packDirName = FMUM.MODID;
	
	/// Configurations ///
	/**
	 * Max layers of the modules x 2(0-255 x 2 = 0-510)
	 */
	public static int maxLocLen = 16;
	
	public static int maxCanInstall = 5;
	
	@SideOnly(Side.SERVER)
	private static String localizeFileName;
	
	/**
	 * For server side localization
	 */
	@SideOnly(Side.SERVER)
	private static HashMap<String, String> localizationMap;
	
	public void syncConfig(Configuration config, File minecraftDir)
	{
		// Load localization map server side
		localizeFileName = config.getString(
			"localizationFile",
			"Server Only",
			"en_us.lang",
			"Server side localization file"
		);
		mcDir = minecraftDir;
		this.loadLocalizationMap();
		
		// Parse configuration
		this.parseConfig(config);
		
		// Save configuration file if has changed
		if(config.hasChanged())
			config.save();
	}
	
	public void loadLocalizationMap()
	{
		// TODO Add server side localization
		final HashMap<String, String> m = new HashMap<>();
		
		
		
		localizationMap = m;
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
	
	public void checkOpenGL() { }
	
	public void loadContentPack()
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
		
		// Call post process for types requires this
		for(TypeInfo t : typesWaitForPostLoadProcess)
			t.postLoad();
	}
	
	/**
	 * Called in client side to prepare tab icon item
	 */
	public void setupCreativeTabs() { }
	
	/**
	 * Called in client side to trigger key lazy load and load key binds
	 */
	public void loadKeyBinds() { }
	
	public void registerLocalResource(File source)
	{
		try { FMUMClassLoader.INSTANCE.addURL(source.toURI().toURL()); }
		catch(MalformedURLException e) {
			FMUM.log.error(this.format("fmum.erroraddingclasspath", source.getName()), e);
		}
	}
	
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
	
	public Model loadModel(String modelPath) { return null; }
	
	protected void parseConfig(Configuration config)
	{
		final String COMMON_SETTING = "Common";
		
		packDirName = config.getString(
			"packFolderName",
			COMMON_SETTING,
			packDirName,
			"Content pack folder name where FMUM will load content packs from"
		);
		maxLocLen = config.getInt(
			"maxLayers",
			COMMON_SETTING,
			maxLocLen >>> 1,
			1,
			255,
			"Max layers of modules can be installed on a base module"
		) << 1;
		maxCanInstall = config.getInt(
			"maxCanInstall",
			COMMON_SETTING,
			maxCanInstall,
			1,
			255,
			"Max number of modules that can be installed in a single slot"
		);
	}
}
