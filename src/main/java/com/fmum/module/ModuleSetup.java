package com.fmum.module;

import com.google.gson.annotations.Expose;
import com.mojang.realmsclient.util.Pair;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ModuleSetup
{
	@Expose
	protected List< Map< String, ModuleSetup > > slots = Collections.emptyList();
	
	@Expose
	protected short paintjob_idx = 0;
	
	
	public IModule build( IModule module, Function< String, Optional< IModule > > module_factory )
	{
		final IModule finished = this._setupModule( module );
		
		// Install modules.
		IntStream.range( 0, this.slots.size() )
			.mapToObj( slot_idx -> Pair.of(
				slot_idx,
				this.slots.get( slot_idx ).entrySet().stream()
					.map( e -> Pair.of( module_factory.apply( e.getKey() ), e.getValue() ) )
					.filter( p -> p.first().isPresent() )
					.map( e -> e.second().build( e.first().get(), module_factory ) )
					.collect( Collectors.toList() )
			) )
			.forEachOrdered( pair -> {
				final int slot_idx = pair.first();
				final List< IModule > mods = pair.second();
				mods.forEach( mod -> finished.tryInstall( slot_idx, mod ).apply() );
			} );
		
		return finished;
	}
	
	protected IModule _setupModule( IModule module )
	{
		module.trySetPaintjob( this.paintjob_idx ).apply();
		return module;
	}
}
