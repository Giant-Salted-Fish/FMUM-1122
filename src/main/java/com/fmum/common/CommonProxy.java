package com.fmum.common;

import java.io.File;
import java.net.MalformedURLException;
import java.util.LinkedList;
import java.util.TreeMap;
import java.util.regex.Pattern;

import com.fmum.common.pack.FMUMContentProvider;
import com.fmum.common.pack.FolderContentPack;
import com.fmum.common.pack.ZipContentPack;

import net.minecraft.client.resources.I18n;
import net.minecraftforge.common.MinecraftForge;

public class CommonProxy
{
	/**
	 * All content packs loaded for {@link FMUM}
	 */
	public static final TreeMap<String, FMUMContentProvider> contentProviders = new TreeMap<>();
	
	protected static final Pattern SUFFIX_ZIP_JAR = Pattern.compile("(.+)\\.(zip|jar)$");
	
	public void registerEventListener()
	{
		MinecraftForge.EVENT_BUS.register(new ForgeEventListener());
	}
	
	public final void loadContentPack(File dir)
	{
		// Find all content providers
		final LinkedList<FMUMContentProvider> providers = new LinkedList<>();
		for(File file : dir.listFiles())
		{
			FMUMContentProvider provider;
			if(file.isDirectory())
				provider = new FolderContentPack(file);
			else if(SUFFIX_ZIP_JAR.matcher(file.getName()).matches())
				provider = new ZipContentPack(file);
			else
			{
				FMUM.log.warn(
					I18n.format(
						"fmum.unrecognizedcontentpackfiletype",
						dir.getName() + "/" + file.getName()
					)
				);
				continue;
			}
			
			providers.add(provider);
			FMUM.log.info(I18n.format("fmum.detectcontentpack", provider.getSourceName()));
		}
		
		// Load packs!
		for(FMUMContentProvider fcm : providers)
		{
			fcm.prepareLoad();
			FMUM.log.info(I18n.format("fmum.preloadcontentpack", fcm.getSourceName()));
		}
		for(FMUMContentProvider fcm : providers)
		{
			fcm.loadContents();
			contentProviders.put(fcm.getName(), fcm);
			FMUM.log.info(I18n.format("fmum.loadedcontentpack", fcm.getSourceName()));
		}
	}
	
	public void registerLocalResource(File source)
	{
		try { FMUMClassLoader.instance.addURL(source.toURI().toURL()); }
		catch(MalformedURLException e) {
			FMUM.log.error(I18n.format("fmum.erroraddingclasspath", source.getName()), e);
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
		FMUM.log.info(I18n.format("fmum.infoactivedpacks", contentProviders.size()));
		for(FMUMContentProvider pack : contentProviders.values())
			FMUM.log.info(
				I18n.format(
					"fmum.activedpackinfo",
					I18n.format(pack.getName()),
					I18n.format(pack.getAuthor())
				)
			);
	}
}
