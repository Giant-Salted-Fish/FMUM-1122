package com.mcwb.client.input;

import com.mcwb.common.meta.IMeta;
import com.mcwb.common.pack.IContentProvider;

public class ExternalKeyBind extends KeyBind
{
	protected String updateGroup = "default";
	
	@Override
	public IMeta build( String name, IContentProvider provider )
	{
		super.build( name, provider );
		
		this.setupUpdateGroup();
		return this;
	}
	
	protected void setupUpdateGroup()
	{
		switch( this.updateGroup.toLowerCase() )
		{
		case "global":
		case "always":
		case "universal":
			InputHandler.GLOBAL_KEYS.add( this );
			break;
			
		case "trigger":
		case "normal":
		case "default":
		case "press":
			InputHandler.INCO_KEYS.add( this );
			break;
			
		case "co":
		case "cokey":
		case "assist":
			InputHandler.CO_KEYS.add( this );
			break;
			
		default: this.error( "mcwb.can_not_find_update_group", this, this.updateGroup );
		}
	}
}
