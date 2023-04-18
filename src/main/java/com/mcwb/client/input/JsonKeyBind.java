package com.mcwb.client.input;

import com.mcwb.common.load.BuildableLoader;
import com.mcwb.common.load.IContentProvider;
import com.mcwb.common.meta.IMeta;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly( Side.CLIENT )
public class JsonKeyBind extends KeyBind
{
	public static final BuildableLoader< IMeta >
		LOADER = new BuildableLoader<>( "key_bind", JsonKeyBind.class );
	
	protected String updateGroup = "default";
	
	@Override
	public IMeta build( String name, IContentProvider provider )
	{
		super.build( name, provider );
		
		switch ( this.updateGroup.toLowerCase() )
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
			
		default: this.logError( "mcwb.can_not_find_update_group", this, this.updateGroup );
		}
		return this;
	}
	
	@Override
	protected IMeta descriptor() { return LOADER; }
}
