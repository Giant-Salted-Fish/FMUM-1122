package com.fmum.common.pack;

import com.fmum.common.pack.IContentPackFactory.IPostLoadContext;
import com.google.gson.Gson;

import java.util.function.Consumer;

public interface IContentBuildContext
{
	String fallbackName();
	
	IContentPack contentPack();
	
	Gson gson();
	
	void regisPostLoadCallback( Consumer< IPostLoadContext  > callback );
	
//	public abstract ResourceLocation loadTexture( String path );
}
