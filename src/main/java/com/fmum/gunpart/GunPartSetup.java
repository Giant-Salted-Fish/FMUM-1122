package com.fmum.gunpart;

import com.fmum.module.IModule;
import com.fmum.module.ModuleSetup;
import com.google.gson.annotations.Expose;

public class GunPartSetup extends ModuleSetup
{
	@Expose
	protected short offset = 0;
	
	@Expose
	protected short step = 0;
	
	@Override
	protected IModule _setupModule( IModule module )
	{
		final IGunPart part = ( IGunPart ) module;
		part.trySetOffsetAndStep( this.offset, this.step ).apply();
		return super._setupModule( part );
	}
}
