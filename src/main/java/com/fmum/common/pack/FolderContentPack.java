package com.fmum.common.pack;

import java.io.File;

import com.fmum.common.FMUM;
import com.fmum.common.type.EnumType;
import com.fmum.common.type.TypeInfo;
import com.fmum.common.type.TypeTextParser.LocalTypeFileParser;

import net.minecraft.client.resources.I18n;

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
	
	public FolderContentPack(File dir)
	{
		super(dir);
	}
	
	@Override
	public void loadContents()
	{
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
					if(fName.endsWith(TXT_SUFFIX))
						this.curParser.parse(
							typeFile,
							this.getSourceName(),
							superClassPath
						).postParse();
					// Load typer by class file
					else if(fName.endsWith(CLASS_SUFFIX))
						tryInstantiate(fName, superClassPath, this.getSourceName());
					else
						FMUM.log.error(
							I18n.format(
								"fmum.unrecognizedtypefiletype",
								this.getSourceName() + ":" + superClassPath + "." + fName
							)
						);
				}
			);
		}
		
		// Load creative tabs
		File tabDir = new File(this.source, FMUMCreativeTab.RECOMMENDED_SOURCE_DIR_NAME);
		if(!tabDir.exists() || !tabDir.isDirectory()) return;
		
		iterateTypeFiles(
			tabDir,
			(typeFile, superClassPath) -> {
				final String fName = typeFile.getName();
				if(fName.endsWith(CLASS_SUFFIX))
				{
					tryInstantiate(fName, superClassPath, this.getSourceName());
					return;
				}
				
				// Create a tab in name of the file name
				new FMUMCreativeTab(fName, this.getSourceName());
			}
		);
	}
}
