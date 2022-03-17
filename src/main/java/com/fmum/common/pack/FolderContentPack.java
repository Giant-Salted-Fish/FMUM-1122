package com.fmum.common.pack;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import com.fmum.common.FMUM;
import com.fmum.common.pack.FMUMCreativeTab.IconBasedTab;
import com.fmum.common.type.EnumType;
import com.fmum.common.type.TypeInfo;
import com.fmum.common.type.TypeTextParser.LocalTypeFileParser;
import com.fmum.common.util.Messager;

/**
 * Abstraction of the packs that provide content
 * 
 * @author Giant_Salted_Fish
 */
public final class FolderContentPack extends LocalContentProvider
{
	/**
	 * Parser that is currently used to parse plain type file
	 */
	protected LocalTypeFileParser<? extends TypeInfo> curParser = null;
	
	public FolderContentPack(File dir) { super(dir); }
	
	@Override
	public void loadContents()
	{
		// Read pack info
		File infoFile = new File(this.source, ContentProviderSourceInfo.RECOMMENDED_INFO_FILE_NAME);
		Messager sourceTrace = () -> this.source.getName() + "." + infoFile.getName();
		if(infoFile.exists())
			try(BufferedReader in = new BufferedReader(new FileReader(infoFile))) {
				this.parseInfo(in, sourceTrace);
			}
			catch(IOException e) { printIOError(sourceTrace.message(), e); }
		
		// Check each type folder
		for(EnumType type : EnumType.values())
		{
			File typeDir = new File(this.source, type.recommendedSourceDirName);
			if(!typeDir.exists() || !typeDir.isDirectory()) continue;
			
			// Set parser before processing type files
			this.curParser = type.parser;
			iterateTypeFiles(
				typeDir,
				(typeFile, superClassPath) -> {
					final String fName = typeFile.getName();
					final Messager fileTrace
						= () -> this.getSourceName() + "." + superClassPath + "." + fName;
					
					if(fName.endsWith(FMUM.TXT_FILE_SUFFIX))
						try
						{
							this.curParser.parse(
								typeFile,
								fName.substring(0, fName.length() - FMUM.TXT_FILE_SUFFIX.length()),
								fileTrace
							).noticeProvider(this).postParse();
						}
						catch(IOException e) { printIOError(fileTrace.message(), e); }
					// Load typer by class file
					else if(fName.endsWith(FMUM.CLASS_FILE_SUFFIX))
						this.loadClassBasedTyper(fileTrace, superClassPath, fName);
					else printUnrecognizedType(fileTrace.message());
					return false;
				}
			);
		}
		
		// Load creative tabs if has
		File tabDir = new File(this.source, FMUMCreativeTab.RECOMMENDED_SOURCE_DIR_NAME);
		if(!tabDir.exists() || !tabDir.isDirectory()) return;
		
		iterateTypeFiles(
			tabDir,
			(tabFile, superClassPath) -> {
				final String fName = tabFile.getName();
				final Messager st = () -> this.getSourceName() + ":" + superClassPath + "." + fName;
				
				// Create a tab in name of the file name if it is the not class based tab
				if(!fName.endsWith(FMUM.CLASS_FILE_SUFFIX))
					try { IconBasedTab.parser.parse(tabFile, tabFile.getName(), st); }
					catch(IOException e) { printIOError(st.message(), e); }
				
				else FMUM.tryInstantiate(superClassPath, fName);
				
				return false;
			}
		);
	}
}
