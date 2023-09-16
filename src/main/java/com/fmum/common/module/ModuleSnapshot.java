package com.fmum.common.module;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public class ModuleSnapshot
{
	protected static final Function< String, Optional< IModule< ? > > > FACTORY =
		name -> IModuleType.REGISTRY.lookup( name ).map( IModuleType::createRawModule );
	
	protected String module = "unspecified";
	
	protected List< List< ModuleSnapshot > > slots = Collections.emptyList();
	
	protected short offset = 0;
	protected short step = 0;
	
	protected short paintjob = 0;
	
	public < T extends IModule< ? > > Optional< T > setSnapshot(
		Function< String, Optional< T > > module_factory
	) {
		final Optional< T > module = module_factory.apply( this.module );
		module.ifPresent( mod -> {
			final IModuleModifySession< ? > session = mod.newModifySession();
			
			// Setup settings.
			session.setOffsetAndStep( this.offset, this.step );
			session.setPaintjob( this.paintjob );
			
			// Install modules.
			for ( int i = 0, size = this.slots.size(); i < size; i += 1 )
			{
				final int islot = i;
				this.slots.get( islot ).forEach( snapshot -> {
					final Optional< IModule< ? > > child = snapshot.setSnapshot( FACTORY );
					child.ifPresent( child_mod -> session.install( islot, child_mod, idx -> { } ) );
				} );
			}
			
			// We do not care if modification is valid or not for snapshot.
			session.apply();
		} );
		return module;
	}
}
