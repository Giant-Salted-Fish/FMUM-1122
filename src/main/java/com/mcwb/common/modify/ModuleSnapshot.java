package com.mcwb.common.modify;

import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;

import javax.annotation.Nullable;

import com.google.gson.annotations.SerializedName;
import com.mcwb.common.IAutowireLogger;

import net.minecraft.nbt.NBTTagCompound;

public class ModuleSnapshot implements IModuleSnapshot, IAutowireLogger
{
	public static final ModuleSnapshot DEFAULT = new ModuleSnapshot();
	
	protected static final BiFunction< String, NBTTagCompound, IContextedModifiable >
		CONTEXTED_GETTER = ( name, nbt ) -> {
			final IModifiableMeta meta = IModifiableMeta.REGISTRY.get( name );
			return meta != null ? meta.newContexted( nbt ) : null;
		};
	
	@SerializedName( value = "module", alternate = "name" )
	protected String module = "unspecified";
	
	@SerializedName( value = "slots", alternate = "installeds" )
	protected List< List< IModuleSnapshot > > slots = Collections.emptyList();
	
	protected short step;
	protected short offset;
	
	@SerializedName( value = "paintjob", alternate = { "damage", "meta" } )
	protected short paintjob;
	
	@Nullable
	@Override
	public < T extends IContextedModifiable > T initContexted(
		BiFunction< String, NBTTagCompound, T > getter
	) {
		final T module = getter.apply( this.module, new NBTTagCompound() );
		if( module == null )
		{
			this.error( "mcwb.fail_to_find_module", this.module );
			return null;
		}
		
		// Setup settings
		module.$step( this.step );
		module.$offset( this.offset );
		module.$paintjob( this.paintjob );
		
		// Install modules
		for( int i = 0, isize = this.slots.size(); i < isize; ++i )
		{
			final int slot = i;
			this.slots.get( i ).forEach( snapshot -> {
				final IContextedModifiable ctxed = snapshot.initContexted( CONTEXTED_GETTER );
				if( ctxed != null )
					module.install( slot, ctxed );
			} );
		}
		return module;
	}
}
