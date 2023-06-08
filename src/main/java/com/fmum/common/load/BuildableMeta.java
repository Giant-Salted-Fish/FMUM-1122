package com.fmum.common.load;

import com.fmum.common.meta.IMeta;
import com.fmum.common.meta.Meta;

public abstract class BuildableMeta extends Meta implements IBuildable< IMeta >
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
