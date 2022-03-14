package com.fmum.common.pack;

import java.io.File;

import org.apache.commons.io.FileUtils;

import com.fmum.common.type.EnumType;

/**
 * Abstraction of the packs that provide content
 * 
 * @author Giant_Salted_Fish
 */
public final class FolderContentPack extends LocalContentProvider
{
	public FolderContentPack(File dir)
	{
		super(dir);
	}
	
	@Override
	public void load()
	{
		// Check each type folder
		for(EnumType type : EnumType.values())
		{
			File typeDir = new File(this.source, type.recommendedSourceDirName);
			if(!typeDir.exists()) continue;
			
			for(File typeFile : FileUtils.listFiles(typeDir, new String[] { "txt", "class" }, true))
			{
				if(typeFile.isDirectory()) continue;
				
				
			}
		}
	}
}
