package com.mcwb.common.modify;

import java.util.Collections;
import java.util.Set;

import com.google.gson.annotations.SerializedName;
import com.mcwb.util.Mat4f;
import com.mcwb.util.Vec3f;

public class SimpleSlot implements IModuleSlot
{
	/**
	 * Modules in this set will be acceptable to be installed into this slot
	 */
	@SerializedName( value = "moduleWhitelist", alternate = "allowedModules" )
	protected Set< String > moduleWhitelist = Collections.emptySet();
	
	/**
	 * Modules in this set is banned by this slot
	 */
	@SerializedName( value = "moduleBlacklist", alternate = "bannedModules" )
	protected Set< String > moduleBlacklist = Collections.emptySet();
	
	/**
	 * The given module is acceptable if its group exists in this white list and itself does not
	 * appear in {@link #moduleBlacklist}
	 */
	@SerializedName( value = "categoryWhitelist", alternate = { "allowed", "allowedCategories" } )
	protected Set< String > categoryWhitelist = Collections.emptySet();
	
	/**
	 * Maximum number of modules can be installed into this slot
	 */
	@SerializedName( value = "capacity", alternate = "maxCanInstall" )
	public byte capacity = 1;
	/**
	 * Coordinate of the root of the slot
	 */
	
	@SerializedName( value = "pos", alternate = "origin" )
	protected Vec3f pos = Vec3f.ORIGIN;
	
	@Override
	public boolean isAllowed( IContextedModifiable module )
	{
		final String name = module.name();
		final String group = module.category();
		return(
			this.moduleWhitelist.size() > 0
			? this.moduleWhitelist.contains( name )
			: (
				!this.moduleBlacklist.contains( name )
				&& this.categoryWhitelist.contains( group )
			)
		);
	}
	
	@Override
	public int capacity() { return this.capacity; }
	
	@Override
	public void scale( float scale ) { this.pos.scale( scale ); }
	
	@Override
	public void applyTransform( IContextedModifiable installed, Mat4f dst ) {
		dst.translate( this.pos );
	}
}
