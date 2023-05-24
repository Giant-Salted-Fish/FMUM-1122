package com.fmum.common.module;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import javax.annotation.Nonnull;

import com.fmum.common.IAutowireLogger;
import com.fmum.common.paintjob.IPaintable;
import com.google.gson.annotations.SerializedName;

/**
 * Provide additional support for {@link IPaintable}
 * 
 * @author Giant_Salted_Fish
 */
public class ModuleSnapshot implements IAutowireLogger
{
	public static final ModuleSnapshot DEFAULT = new ModuleSnapshot();
	
	protected static final Function< String, IModule< ? > > SUPPLIER = name -> {
		final IModuleType type = IModuleType.REGISTRY.get( name );
		return type != null ? type.newRawContexted() : null;
	};
	
	protected String module = "unspecified";
	
	@SerializedName( value = "slots", alternate = "installeds" )
	protected List< List< ModuleSnapshot > > slots = Collections.emptyList();
	
	protected short offset;
	protected short step;
	
	@SerializedName( value = "paintjob", alternate = { "damage", "meta" } )
	protected short paintjob;
	
	public < T extends IModule< ? > > void setSnapshot(
		@Nonnull Function< String, T > supplier,
		Consumer< T > action
	) {
		final T module = supplier.apply( this.module );
		if ( module == null )
		{
			this.logError( "fmum.fail_to_find_module", this.module );
			return;
		}
		
		// Setup settings.
		module.setOffsetStep( this.offset, this.step );
		if ( module instanceof IPaintable ) {
			( ( IPaintable ) module ).setPaintjob( this.paintjob );
		}
		
		// Install modules.
		for ( int i = 0, size = this.slots.size(); i < size; ++i )
		{
			final int slot = i;
			this.slots.get( i ).forEach( snapshot -> {
				snapshot.setSnapshot( SUPPLIER, mod -> module.install( slot, mod ) );
				// This is the special case that we do not use #tryInstall(...)
			} );
		}
		action.accept( module );
	}
}
