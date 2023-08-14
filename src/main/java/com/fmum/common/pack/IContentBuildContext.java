package com.fmum.common.pack;

import com.google.gson.Gson;

public interface IContentBuildContext
{
	String fallbackName();
	
	IContentPack contentPack();
	
	Gson gson();
	
	void regisPostLoadCallback( Runnable callback );
	
//	public abstract ResourceLocation loadTexture( String path );
}
