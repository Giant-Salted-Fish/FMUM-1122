package com.fmum.common.module;

import org.omg.PortableInterceptor.ServerRequestInfo;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public class ModuleSnapshot
{
	public static final ModuleSnapshot DEFAULT = new ModuleSnapshot();
	
	protected static final Function< String, Optional< IModule< ? > > > FACTORY =
		name -> IModuleType.REGISTRY.lookup( name ).map( IModuleType::createRawModule );
	
	
	protected String module = "unspecified";
	
	protected List< List< ModuleSnapshot > > slots = Collections.emptyList();
	
	protected short offset = 0;
	protected short step = 0;
	
	protected short paintjob = 0;
	
	public void restore(
		IModule< ? > root_module,
		Function< String, Optional< IModule< ? > > > module_factory
	) {
		this._restore( new Function< String, Optional< IModule< ? > > >() {
			private Function< String, Optional< IModule< ? > > > delegate = name -> {
				this.delegate = module_factory;
				return Optional.of( root_module );
			};
			
			@Override
			public Optional< IModule< ? > > apply( String name ) {
				return this.delegate.apply( name );
			}
		} );
	}
	
	protected Optional< IModule< ? > > _restore(
		Function< String, Optional< IModule< ? > > > module_factory
	) {
		final Optional< IModule< ? > > module = module_factory.apply( this.module );
		module.ifPresent( mod -> {
			final IModuleModifySession< ? > session = mod.openModifySession();
			
			// Setup settings.
			session.setOffsetAndStep( this.offset, this.step );
			session.setPaintjob( this.paintjob );
			
			// Install modules.
			for ( int i = 0, size = this.slots.size(); i < size; i += 1 )
			{
				final int islot = i;
				this.slots.get( islot ).forEach( snapshot -> {
					final Optional< IModule< ? > > child = snapshot._restore( module_factory );
					child.ifPresent( child_mod -> session.install( islot, child_mod, idx -> { } ) );
				} );
			}
			
			// We do not care if modification is valid or not for snapshot.
			session.commit();
		} );
		return module;
	}
}
