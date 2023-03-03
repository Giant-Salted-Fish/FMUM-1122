package com.mcwb.common.module;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import javax.annotation.Nullable;

import com.google.gson.annotations.SerializedName;
import com.mcwb.common.IAutowireLogger;
import com.mcwb.common.paintjob.IPaintable;

/**
 * Provide additional support for {@link IPaintable}
 * 
 * @author Giant_Salted_Fish
 */
public class ModuleSnapshot implements IAutowireLogger
{
	public static final ModuleSnapshot DEFAULT = new ModuleSnapshot();
	
	protected static final Function< String, IModular< ? > > SUPPLIER = name -> {
		final IModularType type = IModularType.REGISTRY.get( name );
		return type != null ? type.newPreparedContexted() : null;
	};
	
	protected String module = "unspecified";
	
	@SerializedName( value = "slots", alternate = "installeds" )
	protected List< List< ModuleSnapshot > > slots = Collections.emptyList();
	
	protected short offset;
	protected short step;
	
	@SerializedName( value = "paintjob", alternate = { "damage", "meta" } )
	protected short paintjob;
	
	@Nullable
	public < T extends IModular< ? > > T setSnapshot( Function< String, T > supplier )
	{
		final T module = supplier.apply( this.module );
		if( module == null )
		{
			this.error( "mcwb.fail_to_find_module", this.module );
			return null;
		}
		
		// Setup settings
		module.updateOffsetStep( this.offset, this.step );
		if( module instanceof IPaintable )
			( ( IPaintable ) module ).updatePaintjob( this.paintjob );
		
		// Install modules
		for( int i = 0, size = this.slots.size(); i < size; ++i )
		{
			final int slot = i;
			this.slots.get( i ).forEach( snapshot -> {
				final IModular< ? > tarMod = snapshot.setSnapshot( SUPPLIER );
				if( tarMod != null ) module.install( slot, tarMod );
				// This is the special case that we do not use tarMod.installTo(...)
			} );
		}
		return module;
	}
}
