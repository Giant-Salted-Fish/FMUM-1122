package com.fmum.common.pack;

import java.util.Map;

import javax.annotation.Nullable;

import com.fmum.common.type.TypeTextParser.LocalTypeFileParser;

public interface FMUMContentProvider
{
	/**
	 * Prepare resources for the later loading. Register class path, assist locations.
	 */
	public void prepareLoad();
	
	/**
	 * Load contents in this content provider. Called after {@link #prepareLoad()}.
	 */
	public void loadContents();
	
	/**
	 * @return Name of the content provider that can uniquely identify this provider
	 */
	default public String getName() { return this.getInfo().name; }
	
	/**
	 * @return Name of the content provider author
	 */
	default public String getAuthor() { return this.getInfo().author; }
	
	/**
	 * @return
	 *     Name of source where this content provider fetches content from. Usually is the name of
	 *     the .zip|jar file or folder on local disk.
	 */
	public String getSourceName();
	
	/**
	 * @return Additional meta data for this content pack
	 */
	@Nullable
	default public Map<String, ?> getMeta() { return null; }
	
	/**
	 * @return Information about the source that provides the content
	 */
	public ContentProviderSourceInfo getInfo();
	
	/**
	 * A default implementation for content pack source provider info. A default {@link #parser}
	 * has been provided for convenient info reading. 
	 * 
	 * @author Giant_Salted_Fish
	 */
	public static class ContentProviderSourceInfo
	{
		public static final String RECOMMENDED_INFO_FILE_NAME = "pack.txt";
		
		public static final LocalTypeFileParser<ContentProviderSourceInfo>
			parser = new LocalTypeFileParser<>(ContentProviderSourceInfo.class, null);
		static
		{
			parser.addKeyword("Name", (s, t) -> t.name = s[1]);
			parser.addKeyword("Author", (s, t) -> t.author = s[1]);
			parser.addKeyword("Description", (s, t) -> t.description = s[1]);
		}
		
		public String name;
		
		public String author = "pack.authormissing";
		
		public String description = "pack.descriptionmissing";
		
		public ContentProviderSourceInfo(String name) { this.name = name; }
		
		public ContentProviderSourceInfo(String name, String source) { this.name = name; }
	}
}
