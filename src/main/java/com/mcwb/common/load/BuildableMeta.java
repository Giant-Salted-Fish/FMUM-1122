package com.mcwb.common.load;

import com.mcwb.common.IAutowireLogger;
import com.mcwb.common.meta.IMeta;
import com.mcwb.common.meta.Meta;

public abstract class BuildableMeta extends Meta implements IBuildable< IMeta >, IAutowireLogger
{
	protected transient IContentProvider provider;
	
	@Override
	public IMeta build( String name, IContentProvider provider )
	{
		this.name = this.name != null ? this.name : name;
		this.provider = provider;
		return this;
	}
	
	@Override
	public String toString() { return this.descriptor() + "::" + this.provider + "." + this.name; }
	
	/**
	 * @return Usually is the corresponding {@link BuildableLoader}.
	 */
	protected abstract IMeta descriptor();
}
