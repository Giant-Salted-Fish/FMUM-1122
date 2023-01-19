package com.mcwb.common.item;

import com.google.gson.annotations.SerializedName;
import com.mcwb.client.item.IItemModel;
import com.mcwb.common.meta.ICategoried;
import com.mcwb.common.meta.IMeta;
import com.mcwb.common.pack.IContentProvider;

public abstract class CategoriedItemType<
	C extends IContextedItem,
	M extends IItemModel< ? super C >
> extends ItemMeta< C, M > implements ICategoried
{
	@SerializedName( value = "category", alternate = "group" )
	protected String category;
	
	@Override
	public IMeta build( String name, IContentProvider provider )
	{
		super.build( name, provider );
		
		// If category is not set then set it to its name
		if( this.category == null )
			this.category = this.name;
		return this;
	}
	
	@Override
	public String category() { return this.category; }
}
