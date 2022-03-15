package com.fmum.common;

import java.io.File;
import java.net.MalformedURLException;
import java.util.LinkedList;
import java.util.regex.Pattern;

import com.fmum.common.pack.FMUMContentProvider;
import com.fmum.common.pack.FolderContentPack;

import net.minecraft.client.resources.I18n;
import net.minecraftforge.common.MinecraftForge;

public class CommonProxy
{
	protected static final Pattern SUFFIX_JAR_ZIP = Pattern.compile("(.+)\\.(jar|zip)$");
	
	public void registerEventListener()
	{
		MinecraftForge.EVENT_BUS.register(new ForgeEventListener());
	}
	
	public final void loadContentPack(File dir)
	{
		// Find all content providers
		final LinkedList<FMUMContentProvider> contentProviders = new LinkedList<>();
		for(File file : dir.listFiles())
		{
			if(file.isDirectory())
				contentProviders.add(new FolderContentPack(file));
			else if(SUFFIX_JAR_ZIP.matcher(file.getName()).matches())
//				contentPacks.add(new ZipContentPack(file))
				;
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
			
			FMUM.log.info(I18n.format("fmum.detectcontentpack", file.getName()));
		}
		
		for(FMUMContentProvider fcm : contentProviders)
		{
			fcm.prepareLoad();
			FMUM.log.info(I18n.format("fmum.preloadcontentpack", fcm.getSourceName()));
		}
		for(FMUMContentProvider fcm : contentProviders)
		{
			fcm.loadContents();
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
}
