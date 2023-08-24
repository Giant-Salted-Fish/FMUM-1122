package com.fmum.common.load;

import com.fmum.common.pack.ContentPack;
import com.fmum.common.pack.ContentPackFactory.PostLoadContext;
import com.google.gson.Gson;

import java.util.function.Consumer;

public interface ContentBuildContext
{
	String fallbackName();
	
	/**
	 * The pack that the building content is loaded from.
	 */
	ContentPack contentPack();
	
	Gson gson();
	
	void regisPostLoadCallback( Consumer< PostLoadContext > callback );
	
//	public abstract ResourceLocation loadTexture( String path );
}
