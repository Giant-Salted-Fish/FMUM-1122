package com.fmum.common.load;

import com.fmum.common.pack.IContentPack;
import com.fmum.common.pack.IContentPackFactory.IPostLoadContext;
import com.google.gson.Gson;

import java.util.function.Consumer;

public interface IContentBuildContext
{
	String fallbackName();
	
	/**
	 * The pack that the building content is loaded from.
	 */
	IContentPack contentPack();
	
	Gson gson();
	
	void regisPostLoadCallback( Consumer< IPostLoadContext  > callback );
	
//	public abstract ResourceLocation loadTexture( String path );
}
