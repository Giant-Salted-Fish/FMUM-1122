package com.fmum.load;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.ModContainer;

import java.util.List;

public interface IPackInfo
{
	String getName();
	
	String getAuthor();
	
	/**
	 * Corresponding namespace to construct {@link ResourceLocation}. Usually is
	 * the mod ID of the content pack.
	 */
	String getNamespace();
	
	/**
	 * Used in error handling code to give human-readable hints about the
	 * problem source. Usually is the file name of the pack.
	 */
	String getSourceName();
	
	
	static IPackInfo of( ModContainer container )
	{
		final String modid = container.getModId();
		final List< String > author_lst = container.getMetadata().authorList;
		final String source_name = container.getSource().getName();
		return new IPackInfo() {
			@Override
			public String getName() {
				return modid;
			}
			
			@Override
			public String getAuthor() {
				return String.join( ", ", author_lst );
			}
			
			@Override
			public String getNamespace() {
				return modid;
			}
			
			@Override
			public String getSourceName() {
				return source_name;
			}
			
			@Override
			public String toString() {
				return modid;
			}
		};
	}
}
