package com.fmum.common.load;

@FunctionalInterface
public interface IPostLoadSubscriber
{
	/**
	 * This will be called after all types being loaded and built. Since the order of type load is
	 * not guaranteed, you can delay the initialization task that depends on other types to this
	 * stage. Otherwise, it is recommended to do the initialization as early as possible in
	 * {@link IBuildable#build(String, IContentProvider)} to ensure the integrity of the type.
	 * 
	 * @see IContentProvider#regisPostLoadSubscriber(IPostLoadSubscriber)
	 */
	void onPostLoad();
}
