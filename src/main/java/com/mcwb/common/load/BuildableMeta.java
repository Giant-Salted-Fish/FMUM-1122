package com.mcwb.common.load;

import com.mcwb.common.IAutowireLogger;
import com.mcwb.common.meta.IMeta;
import com.mcwb.common.meta.Meta;
import com.mcwb.common.pack.IContentProvider;

public abstract class BuildableMeta extends Meta implements IBuildable< IMeta >, IAutowireLogger
{
	protected transient IMeta provider;
	
	@Override
	public IMeta build( String name, IContentProvider provider )
	{
		this.name = this.name != null ? this.name : name;
		this.provider = provider;
		return this;
	}
	
	@Override
	public String toString() { return this.loader() + "::" + this.provider + "." + this.name; }
	
	/**
	 * @return The loader of this type. Usually is the corresponding {@link BuildableLoader}.
	 */
	protected abstract IMeta loader();
}
