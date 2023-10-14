package gsf.fmum.common.pack;

import net.minecraft.util.ResourceLocation;

public interface IContentPack
{
	String name();
	
	String author();
	
	/**
	 * Corresponding domain to construct {@link ResourceLocation}.
	 */
	String resourceDomain();

	/**
	 * Used in error handling to give human-readable hints about the problem.
	 */
	String sourceName();
}
