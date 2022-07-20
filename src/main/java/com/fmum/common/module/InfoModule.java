package com.fmum.common.module;

import javax.annotation.Nullable;

import com.fmum.common.util.CoordSystem;
import com.fmum.common.util.ObjPool;

import net.minecraft.nbt.NBTTagList;

/**
 * Helps to keep track of an installed {@link MetaModular}
 * 
 * @author Giant_Salted_Fish
 */
public class InfoModule extends CoordSystem
{
	private static final ObjPool< InfoModule > pool = new ObjPool<>( () -> new InfoModule() );
	
	public NBTTagList tag = null;
	
	public MetaModular meta = null;
	
	public final CoordSystem sys = CoordSystem.locate();
	
	public static InfoModule locate() { return pool.poll(); }
	
	protected InfoModule() { }
	
	/**
	 * Call this to initialize this before you do actual work
	 * 
	 * @param tag Tag of the module
	 * @param meta Meta of the module
	 * @return {@code this}
	 */
	public final InfoModule reset( NBTTagList tag, MetaModular meta )
	{
		this.tag = tag;
		this.meta = meta;
		this.reset();
		return this;
	}
	
	/**
	 * Move one layer deeper to the module installed on current module in given slot and index
	 * 
	 * @return {@code this}
	 */
	public final InfoModule moveTo( int slot, int idx )
	{
		final ModuleSlot moduleSlot = this.meta.slot( slot );
		this.tag = ( NBTTagList ) ( ( NBTTagList) this.tag.get( 1 + slot ) ).get( idx );
		this.meta = MetaModular.decodeInstalled( this.tag );
		
		moduleSlot.apply( this );
		this.meta.apply( this.tag, moduleSlot, this );
		return this;
	}
	
	/**
	 * Move to required module by constantly invoke {@link #moveTo(int, int)}
	 * 
	 * @return {@code this}
	 */
	public final InfoModule moveTo( byte[] loc, int len )
	{
		for( int i = 0; i < len; i += 2 )
			this.moveTo( 0xFF & loc[ i ], 0xFF & loc[ i + 1 ] );
		return this;
	}
	
	@Nullable
	public final InfoModule tryMoveTo( int slot, int idx )
	{
		if( slot >= this.meta.numSlots() ) return null;
		ModuleSlot moduleSlot = this.meta.slot( slot );
		NBTTagList slotTag = ( NBTTagList ) this.tag.get( 1 + slot );
		
		if( idx > slotTag.tagCount() ) return null;
		this.tag = ( NBTTagList ) slotTag.get( idx );
		this.meta = MetaModular.decodeInstalled( this.tag );
		
		moduleSlot.apply( this );
		this.meta.apply( this.tag, moduleSlot, this );
		return this;
	}
	
	@Override
	public void release() { pool.back( this ); }
}
