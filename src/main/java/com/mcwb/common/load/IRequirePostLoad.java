package com.mcwb.common.load;

import com.mcwb.common.meta.IMeta;
import com.mcwb.common.pack.IContentProvider;

@FunctionalInterface
public interface IRequirePostLoad
{
	/**
	 * This will be called after all meta being loaded and built. Since the order of meta load is
	 * not guaranteed, you can delay the initialization task that depends on other meta to this
	 * phase. Otherwise, it is recommended to do the initialization as early as possible in
	 * {@link IBuildable#build(String, IMeta)} to ensure the integrity.
	 * 
	 * @see IContentProvider#regisPostLoad(RequirePostLoad)
	 */
	public void onPostLoad();
}
