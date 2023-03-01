package com.mcwb.common.module;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import javax.annotation.Nullable;

import com.google.gson.JsonDeserializer;
import com.google.gson.annotations.SerializedName;
import com.mcwb.common.IAutowireLogger;
import com.mcwb.common.MCWB;
import com.mcwb.common.paintjob.IPaintable;

import net.minecraft.nbt.NBTTagCompound;

/**
 * Provide additional support for {@link IPaintable}
 * 
 * @author Giant_Salted_Fish
 */
public class ModuleSnapshot implements IModuleSnapshot, IAutowireLogger
{
	public static final JsonDeserializer< IModuleSnapshot >
		ADAPTER = ( json, typeOfT, context ) -> MCWB.GSON.fromJson( json, ModuleSnapshot.class );
	
	public static final ModuleSnapshot DEFAULT = new ModuleSnapshot();
	
	protected static final Function< String, IModular< ? > > SUPPLIER = name -> {
		final IModularType type = IModularType.REGISTRY.get( name );
		return type != null ? type.newContexted() : null;
	};
	protected static final Wrapper< ?, ? > WRAPPER = new Wrapper<>();
	
	protected String module = "unspecified";
	
	@SerializedName( value = "slots", alternate = "installeds" )
	protected List< List< IModuleSnapshot > > slots = Collections.emptyList();
	
	protected short offset;
	protected short step;
	
	@SerializedName( value = "paintjob", alternate = { "damage", "meta" } )
	protected short paintjob;
	
	@Nullable
	@Override
	public < T extends IModular< ? > > T setSnapshot( Function< String, T > supplier )
	{
		final T module = supplier.apply( this.module );
		if( module == null )
		{
			this.error( "mcwb.fail_to_find_module", this.module );
			return null;
		}
		
		// Set a fake wrapper for it to avoid crash on updating properties
		module.setBase( WRAPPER, 0 );
		
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
		
		// Do not forget to clear that fake wrapper before return
		module.setBase( null, 0 );
		return module;
	}
	
	/**
	 * A fake wrapper that is used temporarily to avoid crash
	 * 
	 * @author Giant_Salted_Fish
	 */
	private static final class Wrapper<
		M extends IModular< ? extends M >,
		T extends IModular< M > & IPaintable
	> extends ModuleWrapper< M, T >
	{
		@Override
		public void deserializeNBT( NBTTagCompound nbt ) {
			throw new RuntimeException( "Try to call deserialize NBT on inner wrapper" );
		}
		
		@Override
		public void syncNBTData() { }
	}
}
