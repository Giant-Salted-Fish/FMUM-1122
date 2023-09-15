package com.fmum.common.module;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

public class ModuleSnapshot
{
	protected String module = "unspecified";
	
	protected List< List< ModuleSnapshot > > slots = Collections.emptyList();
	
	protected short offset = 0;
	protected short step = 0;
	
	protected short paintjob = 0;
	
	public < T extends IModule< ? > > void setSnapshot(
		Function< String, Optional< T > > module_factory,
		Consumer< T > action
	) {
		final Optional< T > module = module_factory.apply( this.module );
		module.ifPresent( mod -> {
			mod.writeAccess( ctx -> {
				ctx.setOffsetAndStep( this.offset, this.step );
				ctx.setPaintjob( this.paintjob );
			} );
		} );
	}
}
