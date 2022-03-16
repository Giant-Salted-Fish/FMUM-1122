package com.fmum.common.pack;

import java.io.File;
import java.io.IOException;
import java.util.TreeMap;

import com.fmum.common.pack.FMUMCreativeTab.IconBasedTab;
import com.fmum.common.type.EnumType;

public final class ZipContentPack extends LocalContentProvider
{
	private static final TreeMap<String, EnumType> entryTypeMap = new TreeMap<>();
	static
	{
		for(EnumType t : EnumType.values())
			entryTypeMap.put(t.recommendedSourceDirName, t);
	}
	
	public ZipContentPack(File zip) { super(zip); }
	
	@Override
	public void loadContents()
	{
		// Load pack info
		iterateTypeZipEntries(
			this.source,
			(entry, in) -> {
				if(!ContentProviderSourceInfo.RECOMMENDED_INFO_FILE_NAME.equals(entry.getName()))
					return false;
				
				this.parseInfo(in, this.getSourceName() + ":" + entry.getName());
				return true;
			}
		);
		
		// Load contents
		iterateTypeZipEntries(
			this.source,
			(entry, in) -> {
				// Skip folders
				if(entry.isDirectory()) return false;
				
				// Ignore the entry if it is not in a type folder
				final String entryName = entry.getName();
				final int i = entryName.indexOf('/');
				if(i < 0) return false;
				
				final String
					classPath = entryName.replace('/', '.').substring(
						0,
						entryName.length() - CLASS_SUFFIX.length()
					),
					fName = entryName.substring(
						entryName.lastIndexOf('/') + 1,
						entryName.length() - TXT_SUFFIX.length()
					),
					sourceTrace = this.getSourceName() + ":" + entryName;
				
				EnumType type = entryTypeMap.get(entryName.substring(0, i));
				if(type == null)
				{
					// Not a normal type, check if it is creative tab
					if(entryName.substring(i).equals(FMUMCreativeTab.RECOMMENDED_SOURCE_DIR_NAME))
					{
						if(entryName.endsWith(CLASS_SUFFIX))
							tryInstantiate(classPath, "", sourceTrace);
						
						// Create a tab in name of the file name
						else try {
							IconBasedTab.parser.parse(in, fName, this.getName(), sourceTrace);
						}
						catch(IOException e) { printIOError(sourceTrace, e); }
					}
					
					return false;
				}
				
				// Load typer based on file type
				if(entryName.endsWith(TXT_SUFFIX))
					try { type.parser.parse(in, fName, this.getName(), sourceTrace).postParse(); }
					catch(IOException e) { printIOError(sourceTrace, e); }
				else if(entryName.endsWith(CLASS_SUFFIX))
					tryInstantiate(classPath, "", sourceTrace);
				else printUnrecognizedType(sourceTrace);
				
				return false;
			}
		);
	}
}
