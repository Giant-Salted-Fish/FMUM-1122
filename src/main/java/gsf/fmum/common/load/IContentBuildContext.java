package gsf.fmum.common.load;

import com.google.gson.Gson;
import gsf.fmum.common.pack.IContentPack;
import gsf.fmum.common.pack.IContentPackFactory.IMeshLoadContext;
import gsf.fmum.common.pack.IContentPackFactory.IPostLoadContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.function.Consumer;

public interface IContentBuildContext
{
	String fallbackName();
	
	/**
	 * The pack that the building content is loaded from.
	 */
	IContentPack contentPack();
	
	Gson gson();
	
	void regisPostLoadCallback( Consumer< IPostLoadContext > callback );
	
	@SideOnly( Side.CLIENT )
	void regisMeshLoadCallback( Consumer< IMeshLoadContext > callback );
	
//	public abstract ResourceLocation loadTexture( String path );
}
