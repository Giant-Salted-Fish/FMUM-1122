package com.fmum.common;

import java.io.File;
import java.util.regex.Pattern;

import net.minecraft.client.resources.I18n;

public class CommonProxy
{
	protected static final Pattern SUFFIX_JAR_ZIP = Pattern.compile("(.+)\\.(jar|zip)$");
	
	public void loadContentPack(File dir)
	{
		for(File file : dir.listFiles())
		{
			if(file.isDirectory())
			{
			}
			else if(SUFFIX_JAR_ZIP.matcher(file.getName()).matches())
			{
				
			}
			else
				FMUM.log.warn(
					I18n.format(
						"fmum.unknowncontentpackfiletype",
						dir.getName() + "/" + file.getName()
					)
				);
		}
	}
}
