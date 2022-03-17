package com.fmum.common.pack;

import java.io.File;
import java.io.IOException;
import java.util.TreeMap;

import com.fmum.common.FMUM;
import com.fmum.common.pack.FMUMCreativeTab.IconBasedTab;
import com.fmum.common.type.EnumType;
import com.fmum.common.util.Messager;

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
				
				this.parseInfo(in, () -> this.getSourceName() + ":" + entry.getName());
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
				
				final String fName = entryName.substring(entryName.lastIndexOf('/') + 1);
				final Messager sourceTrace = () -> this.getSourceName() + ":" + entryName;
				
				EnumType type = entryTypeMap.get(entryName.substring(0, i));
				if(type == null)
				{
					// Not a normal type, check if it is creative tab
					if(entryName.substring(i).equals(FMUMCreativeTab.RECOMMENDED_SOURCE_DIR_NAME))
					{
						if(entryName.endsWith(FMUM.CLASS_FILE_SUFFIX))
							FMUM.tryInstantiate(entryName.replace('/', '.'));
						
						// Create a tab in name of the file name
						else try { IconBasedTab.parser.parse(in, fName, sourceTrace); }
						catch(IOException e) { printIOError(sourceTrace.message(), e); }
					}
					
					return false;
				}
				
				// Load typer based on file type
				if(entryName.endsWith(FMUM.TXT_FILE_SUFFIX))
					try
					{
						type.parser.parse(
							in,
							fName.substring(0, fName.length() - FMUM.TXT_FILE_SUFFIX.length()),
							sourceTrace
						).noticeProvider(this).postParse();
					}
					catch(IOException e) { printIOError(sourceTrace.message(), e); }
				else if(entryName.endsWith(FMUM.CLASS_FILE_SUFFIX))
					this.loadClassBasedTyper(sourceTrace, entryName.replace('/', '.'));
				else printUnrecognizedType(sourceTrace.message());
				
				return false;
			}
		);
	}
}
