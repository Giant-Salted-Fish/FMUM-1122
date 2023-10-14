package gsf.fmum.common.load;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

@FunctionalInterface
public interface IContentLoader
{
	Object loadFrom( JsonObject obj, Gson gson, IContentBuildContext ctx );
}
