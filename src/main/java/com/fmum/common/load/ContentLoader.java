package com.fmum.common.load;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

@FunctionalInterface
public interface ContentLoader
{
	Object loadFrom( JsonObject obj, Gson gson, ContentBuildContext ctx );
}
